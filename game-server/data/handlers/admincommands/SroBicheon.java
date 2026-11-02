package admincommands;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.silkroad.skills.BicheonPrototype;
import com.aionemu.gameserver.silkroad.skills.BicheonRepository;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * Admin level 9 ONLY: Bicheon Smash A experimental end-to-end harness.
 * All setup commands affect the GM's OWN character.
 *
 * Requires v001 SQL + -Dsilkroad.bicheon.enabled=true and a test Aion client.
 * The client does not yet have the custom 59001 skill animation or icon.
 */
public class SroBicheon extends AdminCommand {

    public SroBicheon() {
        super("srobicheon", "Silkroad Bicheon PvE prototype (GM test only)", """
            status - Show SP, Bicheon level and learned state.
            setup - Create an empty Chinese test profile for THIS GM.
            sp <amount> - Grant 1..10000 test SP to THIS GM.
            mastery <level> - Set Bicheon level within the character level and mastery cap.
            learn - Spend 2 SP and learn Bicheon Smash A (requires mastery 5).
            cast - Attack the targeted enemy NPC using SkillEngine template 59001.
            """);
    }

    @Override
    public void execute(Player player, String... params) {
        if (!BicheonPrototype.ENABLED) {
            send(player, "Bicheon prototype disabled. Start server with -Dsilkroad.bicheon.enabled=true after running v001 SQL.");
            return;
        }
        if (params.length == 0) {
            sendInfo(player);
            return;
        }
        switch (params[0].toLowerCase(java.util.Locale.ROOT)) {
            case "status" -> status(player);
            case "setup" -> send(player, BicheonRepository.initializeProfile(player.getObjectId())
                    ? "Profile ready; no SP or mastery has been gifted." : "Profile setup failed; check migration and logs.");
            case "sp" -> grantSp(player, params);
            case "mastery" -> setMastery(player, params);
            case "learn" -> learn(player);
            case "cast" -> cast(player);
            default -> sendInfo(player);
        }
    }

    private void status(Player player) {
        BicheonRepository.state(player.getObjectId()).ifPresentOrElse(s ->
                send(player, "Bicheon mastery: " + s.masteryLevel() + "; SP: " + s.skillPoints()
                        + "; Smash A: " + (s.learned() ? "learned" : "not learned")),
                () -> send(player, "No profile found (or SQL unavailable). Run //srobicheon setup first."));
    }

    private void grantSp(Player player, String[] params) {
        if (params.length != 2) {
            sendInfo(player);
            return;
        }
        long amount = Long.parseLong(params[1]);
        send(player, BicheonRepository.grantTestSp(player.getObjectId(), amount)
                ? "Granted " + amount + " test SP." : "Failed to grant SP; max per use is 10000. Setup first.");
    }

    private void setMastery(Player player, String[] params) {
        if (params.length != 2) {
            sendInfo(player);
            return;
        }
        int level = Integer.parseInt(params[1]);
        send(player, BicheonRepository.setTestBicheonLevel(player.getObjectId(), player.getLevel(), level)
                ? "Bicheon test mastery set to " + level
                : "Mastery setup denied: level cap, 300 combined cap, or SQL/profile error.");
    }

    private void learn(Player player) {
        if (com.aionemu.gameserver.dataholders.DataManager.SKILL_DATA.getSkillTemplate(
                BicheonPrototype.AION_SKILL_ID) == null) {
            send(player, "Bicheon XML template is not registered.");
            return;
        }
        // The database commits the SP debit, SRO learned rank AND Aion player_skills row together.
        BicheonRepository.LearnResult result =
                BicheonRepository.learnSmashA(player.getObjectId(), player.getLevel());
        if (result != BicheonRepository.LearnResult.LEARNED) {
            send(player, "Bicheon skill purchase denied: " + result);
            return;
        }
        // SQL is the source of truth and is already committed. This adds the
        // hot in-memory skill and notifies the existing Aion client packet layer.
        player.getSkillList().addSkill(player, BicheonPrototype.AION_SKILL_ID, 1);
        send(player, "Learned Silkroad Bicheon Smash A, cost 2 SP. Aion skill template id: 59001.");
    }

    private void cast(Player player) {
        if (!(player.getTarget() instanceof Npc npc)) {
            send(player, "For this PvE prototype, select a living enemy NPC.");
            return;
        }
        if (!BicheonPrototype.canCast(player)) {
            send(player, "Cannot cast: requires learned Smash A, Bicheon mastery 5 and an Aion SWORD.");
            return;
        }
        if (!PlayerRestrictions.canAttack(player, npc)) {
            send(player, "Invalid PvE target or current combat state.");
            return;
        }
        Skill skill = SkillEngine.getInstance().getSkillFor(
                player, BicheonPrototype.AION_SKILL_ID, npc);
        if (skill == null || !PlayerRestrictions.canUseSkill(player, skill)) {
            send(player, "Bicheon cannot be cast: Aion skill list, cooldown or status denied.");
            return;
        }
        skill.setTargetType(0, 0, 0, 0);
        // Skips client-animation anti-cheat checks; the server STILL validates
        // target, range, costs, mastery and applies physical damage via SkillEngine.
        if (!skill.useNoAnimationSkill()) {
            send(player, "SkillEngine rejected Bicheon cast.");
        }
    }

    private void send(Player player, String message) {
        PacketSendUtility.sendMessage(player, "[Silkroad] " + message);
    }
}
