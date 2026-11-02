package com.aionemu.gameserver.silkroad.monsters;

/**
 * Spawn specification only; must be validated against Aion map data before
 * being installed in the native spawn controller.
 */
public record SilkroadMonsterSpawn(String silkroadCode, int worldId,
        float x, float y, float z, int respawnSeconds, boolean enabled) {

    public SilkroadMonsterSpawn {
        if (silkroadCode == null || !silkroadCode.matches("[A-Z0-9_]{3,128}"))
            throw new IllegalArgumentException("Invalid monster reference");
        if (worldId <= 0 || !Float.isFinite(x) || !Float.isFinite(y) || !Float.isFinite(z))
            throw new IllegalArgumentException("Invalid Aion world coordinates");
        if (respawnSeconds <= 0)
            throw new IllegalArgumentException("Invalid respawn interval");
    }
}
