package com.aionemu.gameserver.silkroad.combat;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * Deliberately opt-in adapter: never reinterpret Silkroad id 3 as Aion id 3.
 * A patched Aion client, skill template, learned skill and persistent profile
 * are prerequisites. The caller MUST verify the authenticated character,
 * ownership, equipped weapon, target, mastery, MP and cooldown through the
 * authoritative game-server checks before invoking prepare().
 */
public final class BicheonAionSkillBridge {
    private BicheonAionSkillBridge() {
    }

    public static Skill prepare(Player player, VisibleObject target,
            int clientVerifiedAionSkillId, BicheonSmashPrototype.CastStatus authorization) {
        if (player == null || target == null || authorization != BicheonSmashPrototype.CastStatus.READY)
            return null;
        // No hardcoded Aion template id is accepted as a Silkroad identifier.
        if (clientVerifiedAionSkillId <= 0)
            return null;
        SkillTemplate template = DataManager.SKILL_DATA.getSkillTemplate(clientVerifiedAionSkillId);
        if (template == null || template.getSkillId() != clientVerifiedAionSkillId)
            return null;
        // This verifies the skill is in the Aion player's learned skill list.
        // It does NOT prove that the client has a matching Silkroad animation.
        return SkillEngine.getInstance().getSkillFor(player, template, target);
    }
}
