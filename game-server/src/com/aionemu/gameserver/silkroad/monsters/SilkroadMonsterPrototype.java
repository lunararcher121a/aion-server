package com.aionemu.gameserver.silkroad.monsters;

/**
 * Silkroad-owned monster definition. An Aion NPC template ID is a visual/
 * engine adapter only, never a Silkroad identity or an imported asset ID.
 * Prototype data does not automatically register NPCs or spawns.
 */
public record SilkroadMonsterPrototype(
        String silkroadCode, String name, int level, long maxHp,
        int physicalAttack, int physicalDefense, Integer aionNpcId, boolean enabled) {

    public SilkroadMonsterPrototype {
        if (silkroadCode == null || !silkroadCode.matches("[A-Z0-9_]{3,128}"))
            throw new IllegalArgumentException("Invalid Silkroad monster code");
        if (name == null || name.isBlank() || name.length() > 128)
            throw new IllegalArgumentException("Invalid monster name");
        if (level < 1 || maxHp < 1 || physicalAttack < 1 || physicalDefense < 0)
            throw new IllegalArgumentException("Invalid monster stats");
        if (aionNpcId != null && aionNpcId <= 0)
            throw new IllegalArgumentException("Invalid Aion NPC template ID");
        if (enabled && aionNpcId == null)
            throw new IllegalArgumentException("Enabled monster requires an Aion NPC mapping");
    }

    public boolean isReadyForAdapter() {
        return enabled && aionNpcId != null;
    }
}
