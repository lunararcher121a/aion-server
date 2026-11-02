package com.aionemu.gameserver.silkroad.combat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;
import com.aionemu.gameserver.silkroad.mastery.ChineseMasteryProgress;
import com.aionemu.gameserver.silkroad.skills.ChineseSkillEligibility.Result;

class BicheonSmashPrototypeTest {
    @Test void requiresBicheonFiveAndTwoSpToTrain() {
        var ready = new ChineseMasteryProgress(2, Map.of(ChineseMastery.BICHEON, 5), 5, 300);
        assertEquals(Result.AVAILABLE, BicheonSmashPrototype.canLearn(ready, group -> 0));
        var poor = new ChineseMasteryProgress(1, Map.of(ChineseMastery.BICHEON, 5), 5, 300);
        assertEquals(Result.INSUFFICIENT_SP, BicheonSmashPrototype.canLearn(poor, group -> 0));
        var low = new ChineseMasteryProgress(100, Map.of(ChineseMastery.BICHEON, 4), 5, 300);
        assertEquals(Result.MASTERY_TOO_LOW, BicheonSmashPrototype.canLearn(low, group -> 0));
    }

    @Test void spIsNotConsumedAtCastTime() {
        var state = new ChineseMasteryProgress(2, Map.of(ChineseMastery.BICHEON, 5), 5, 300);
        assertEquals(BicheonSmashPrototype.CastStatus.READY,
                BicheonSmashPrototype.canCast(true, true, true, 5, 100, 20, true));
        assertEquals(2, state.skillPoints());
    }

    @Test void failsClosedForUnlearnedSkillAndInvalidWeapon() {
        assertEquals(BicheonSmashPrototype.CastStatus.NOT_LEARNED,
                BicheonSmashPrototype.canCast(false, true, true, 5, 100, 20, true));
        assertEquals(BicheonSmashPrototype.CastStatus.WRONG_WEAPON,
                BicheonSmashPrototype.canCast(true, false, true, 5, 100, 20, true));
        assertEquals(BicheonSmashPrototype.CastStatus.INVALID_TARGET,
                BicheonSmashPrototype.canCast(true, true, false, 5, 100, 20, true));
    }

    @Test void checksMasteryMpAndCooldown() {
        assertEquals(BicheonSmashPrototype.CastStatus.MASTERY_TOO_LOW,
                BicheonSmashPrototype.canCast(true, true, true, 4, 100, 20, true));
        assertEquals(BicheonSmashPrototype.CastStatus.NOT_ENOUGH_MP,
                BicheonSmashPrototype.canCast(true, true, true, 5, 19, 20, true));
        assertEquals(BicheonSmashPrototype.CastStatus.ON_COOLDOWN,
                BicheonSmashPrototype.canCast(true, true, true, 5, 100, 20, false));
    }

    @Test void computesBoundedPrototypeBonus() {
        assertEquals(110, BicheonSmashPrototype.calculatePrototypeBonus(1000, 1));
        assertEquals(300, BicheonSmashPrototype.calculatePrototypeBonus(1000, 20));
        assertEquals(0, BicheonSmashPrototype.calculatePrototypeBonus(0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> BicheonSmashPrototype.calculatePrototypeBonus(-1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> BicheonSmashPrototype.calculatePrototypeBonus(10, 0));
        assertTrue(BicheonSmashPrototype.calculatePrototypeBonus(Integer.MAX_VALUE, 20) > 0);
    }
}
