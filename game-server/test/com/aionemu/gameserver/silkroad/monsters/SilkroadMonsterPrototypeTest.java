package com.aionemu.gameserver.silkroad.monsters;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SilkroadMonsterPrototypeTest {
    @Test
    void requiresRealAionMappingBeforeEnable() {
        assertThrows(IllegalArgumentException.class, () ->
                new SilkroadMonsterPrototype("MOB_CH_MANGYANG", "Mangyang", 1, 100, 10, 0, null, true));
        SilkroadMonsterPrototype staged = new SilkroadMonsterPrototype(
                "MOB_CH_MANGYANG", "Mangyang", 1, 100, 10, 0, null, false);
        assertFalse(staged.isReadyForAdapter());
    }

    @Test
    void validatesSpawnBeforeWorldIntegration() {
        assertThrows(IllegalArgumentException.class, () ->
                new SilkroadMonsterSpawn("MOB_CH_MANGYANG", 1, Float.NaN, 1, 1, 60, false));
        assertThrows(IllegalArgumentException.class, () ->
                new SilkroadMonsterSpawn("MOB_CH_MANGYANG", 1, 1, 1, 1, 0, false));
    }
}
