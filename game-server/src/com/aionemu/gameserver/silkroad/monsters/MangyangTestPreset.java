package com.aionemu.gameserver.silkroad.monsters;

/**
 * Experimental Mangyang test preset. Aion Kerubar 210671 is a candidate
 * model, not a confirmed template in this branch/client. Never auto-spawns.
 * Stats below are TEST VALUES, not verified OpenSRO reference values.
 */
public final class MangyangTestPreset {
    public static final int CANDIDATE_AION_NPC_ID = 210671;
    public static final String CODE = "MOB_CH_MANGYANG";

    private MangyangTestPreset() {
    }

    public static SilkroadMonsterPrototype monster() {
        return new SilkroadMonsterPrototype(CODE, "Mangyang (prototype)", 1,
                100, 10, 0, CANDIDATE_AION_NPC_ID, true);
    }

    /**
     * Caller must supply coordinates confirmed on an existing Aion map.
     * No hard-coded invented world position.
     */
    public static SilkroadMonsterSpawn spawnAt(int worldId, float x, float y, float z) {
        return new SilkroadMonsterSpawn(CODE, worldId, x, y, z, 60, true);
    }
}
