package com.aionemu.gameserver.silkroad.skills;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;
import com.aionemu.gameserver.silkroad.mastery.ChineseMasteryProgress;
import com.aionemu.gameserver.silkroad.skills.ChineseSkillEligibility.Result;

class ChineseSkillEligibilityTest {
    private static final IntUnaryOperator NONE_LEARNED = group -> 0;

    @Test
    void allowsSmashWhenMasteryAndSpAreEnough() {
        var profile = new ChineseMasteryProgress(2, Map.of(ChineseMastery.BICHEON, 5), 5, 300);
        assertEquals(Result.AVAILABLE,
                ChineseSkillEligibility.check(ChineseSkillCatalog.find(3).orElseThrow(),
                        profile, NONE_LEARNED));
    }

    @Test
    void deniesMissingMasteryAndInsufficientSp() {
        var smash = ChineseSkillCatalog.find(3).orElseThrow();
        var tooLow = new ChineseMasteryProgress(100, Map.of(ChineseMastery.BICHEON, 4), 10, 300);
        assertEquals(Result.MASTERY_TOO_LOW, ChineseSkillEligibility.check(smash, tooLow, NONE_LEARNED));

        var tooPoor = new ChineseMasteryProgress(1, Map.of(ChineseMastery.BICHEON, 5), 10, 300);
        assertEquals(Result.INSUFFICIENT_SP, ChineseSkillEligibility.check(smash, tooPoor, NONE_LEARNED));
    }

    @Test
    void deniesUnmetPrerequisiteEvenWithEnoughSp() {
        var smashB = ChineseSkillCatalog.find(4).orElseThrow();
        var profile = new ChineseMasteryProgress(500, Map.of(ChineseMastery.BICHEON, 27), 30, 300);

        assertEquals(Result.PREREQUISITE_MISSING,
                ChineseSkillEligibility.check(smashB, profile, group -> group == 174 ? 8 : 0));
        assertEquals(Result.AVAILABLE,
                ChineseSkillEligibility.check(smashB, profile, group -> group == 174 ? 9 : 0));
    }

    @Test
    void deniesTrainingAnAlreadyLearnedRank() {
        var profile = new ChineseMasteryProgress(1000, Map.of(ChineseMastery.BICHEON, 27), 30, 300);
        assertEquals(Result.ALREADY_LEARNED,
                ChineseSkillEligibility.check(ChineseSkillCatalog.find(3).orElseThrow(),
                        profile, group -> group == 174 ? 1 : 0));
    }

    @Test
    void creationSeedDoesNotConsumeSp() {
        var profile = new ChineseMasteryProgress(0, Map.of(), 1, 300);
        for (int id : new int[] {1, 2, 40, 70}) {
            var seed = ChineseSkillCatalog.find(id).orElseThrow();
            assertTrue(seed.isStartingSkill());
            assertEquals(Result.AVAILABLE, ChineseSkillEligibility.check(seed, profile, NONE_LEARNED));
        }
    }

    @Test
    void missingSkillCannotBeInventedByNumericAlias() {
        assertTrue(ChineseSkillCatalog.find(999999).isEmpty());
        assertThrows(NullPointerException.class,
                () -> ChineseSkillEligibility.check(null,
                        new ChineseMasteryProgress(0, Map.of(), 1, 300), NONE_LEARNED));
    }
}
