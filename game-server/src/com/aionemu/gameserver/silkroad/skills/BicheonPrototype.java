package com.aionemu.gameserver.silkroad.skills;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * Explicitly opt-in Bicheon PvE prototype.
 *
 * Enabled with -Dsilkroad.bicheon.enabled=true AFTER applying
 * game-server/sql/silkroad_remastered_v001.sql on a disposable database.
 * No client-side Aion skill id/motion assets have been patched.
 */
public final class BicheonPrototype {
    /** This is an Aion server template id, NEVER a Silkroad skill id. */
    public static final int AION_SKILL_ID = 59001;
    /** Original OpenSRO v1.150 Bicheon Smash A rank 1. */
    public static final int SILKROAD_SKILL_ID = 3;
    /** Aion cooldown field 40 = 4 seconds (unit 100 ms). */
    public static final int PROTOTYPE_COOLDOWN = 40;
    public static final boolean ENABLED = Boolean.getBoolean("silkroad.bicheon.enabled");

    private BicheonPrototype() {
    }

    public static boolean hasSword(Player player) {
        if (player == null || player.getEquipment() == null) {
            return false;
        }
        Item item = player.getEquipment().getMainHandWeapon();
        return item != null && item.getItemTemplate() != null
                && item.getItemTemplate().getItemGroup() == ItemGroup.SWORD;
    }

    /**
     * Enforced on every cast, including alternative server-side SkillEngine
     * entrypoints. A missing migration or database error fails closed.
     * SP is charged when learning, not on each cast.
     */
    public static boolean canCast(Player player) {
        return ENABLED && player != null && hasSword(player)
                && player.getCommonData() != null
                && player.getCommonData().getLevel() >= 5
                && BicheonRepository.isLearnedWithMastery(player.getObjectId());
    }
}
