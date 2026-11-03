package admincommands;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.silkroad.monsters.MangyangTestPreset;
import com.aionemu.gameserver.silkroad.monsters.SilkroadMonsterSpawnAdapter;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * GM level 9 only, opt-in test. The native Aion NPC model must exist.
 * Loaded automatically from data/handlers/admincommands.
 */
public class SroMangyang extends AdminCommand {

    public SroMangyang() {
        super("sromangyang", "Silkroad Mangyang spawn prototype (GM test only)",
                "spawn - Spawn one Mangyang at your current position");
    }

    @Override
    public void execute(Player player, String... params) {
        if (params.length != 1 || !"spawn".equalsIgnoreCase(params[0])) {
            sendInfo(player);
            return;
        }
        if (!Boolean.getBoolean("silkroad.monsters.enabled")) {
            sendInfo(player, "Disabled: enable -Dsilkroad.monsters.enabled=true on a test server.");
            return;
        }
        var pos = player.getPosition();
        Npc npc = SilkroadMonsterSpawnAdapter.spawn(
                MangyangTestPreset.monster(),
                MangyangTestPreset.spawnAt(player.getWorldId(), pos.getX(), pos.getY(), pos.getZ()),
                player.getInstanceId());
        sendInfo(player, "Spawned test Mangyang: object " + npc.getObjectId()
                + ", Aion template " + npc.getNpcId() + ", max HP " + npc.getLifeStats().getMaxHp());
    }
}
