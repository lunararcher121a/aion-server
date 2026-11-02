package com.aionemu.gameserver.silkroad.mastery;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ChineseMasteryProgressTest {
    @Test void buysMasteryLevelWithServerCost() {
        var progress = new ChineseMasteryProgress(100, Map.of(), 10, 20);
        progress.levelUp(ChineseMastery.BICHEON, 10, 20, 35);
        assertEquals(1, progress.level(ChineseMastery.BICHEON));
        assertEquals(65, progress.skillPoints());
    }

    @Test void insufficientSpCannotMutateState() {
        var progress = new ChineseMasteryProgress(10, Map.of(), 10, 20);
        assertThrows(IllegalStateException.class,
            () -> progress.levelUp(ChineseMastery.FIRE, 10, 20, 11));
        assertEquals(0, progress.level(ChineseMastery.FIRE));
        assertEquals(10, progress.skillPoints());
    }

    @Test void respectsCharacterLevelAndMasteryCap() {
        var progress = new ChineseMasteryProgress(100, Map.of(ChineseMastery.BICHEON, 1), 1, 2);
        assertThrows(IllegalStateException.class,
            () -> progress.levelUp(ChineseMastery.BICHEON, 1, 2, 1));
        progress.levelUp(ChineseMastery.FIRE, 1, 2, 1);
        assertThrows(IllegalStateException.class,
            () -> progress.levelUp(ChineseMastery.COLD, 1, 2, 1));
    }

    @Test void rejectsInvalidPersistedStateAndCost() {
        assertThrows(IllegalArgumentException.class,
            () -> new ChineseMasteryProgress(-1, Map.of(), 1, 10));
        assertThrows(IllegalArgumentException.class,
            () -> new ChineseMasteryProgress(0, Map.of(ChineseMastery.COLD, 2), 1, 10));
        var progress = new ChineseMasteryProgress(10, Map.of(), 10, 20);
        assertThrows(IllegalArgumentException.class,
            () -> progress.levelUp(ChineseMastery.COLD, 10, 20, -1));
    }
}
