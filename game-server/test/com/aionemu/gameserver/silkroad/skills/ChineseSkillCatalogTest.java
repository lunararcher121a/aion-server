package com.aionemu.gameserver.silkroad.skills;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;
import com.aionemu.gameserver.skillengine.effect.EffectType;

class ChineseSkillCatalogTest {
    @Test
    void retainsDistinctLocalAndNativeMasteryIdentifiers() {
        assertEquals(1, ChineseMastery.BICHEON.getId());
        assertEquals(257, ChineseMastery.BICHEON.getSilkroadId());
        assertEquals(258, ChineseMastery.HEUKSAL.getSilkroadId());
        assertEquals(259, ChineseMastery.PACHEON.getSilkroadId());
        assertEquals(273, ChineseMastery.COLD.getSilkroadId());
        assertEquals(274, ChineseMastery.LIGHTNING.getSilkroadId());
        assertEquals(275, ChineseMastery.FIRE.getSilkroadId());
        assertEquals(276, ChineseMastery.FORCE.getSilkroadId());
        for (ChineseMastery mastery : ChineseMastery.values()) {
            assertSame(mastery, ChineseMastery.fromId(mastery.getId()));
            assertSame(mastery, ChineseMastery.fromSilkroadId(mastery.getSilkroadId()));
        }
        assertThrows(IllegalArgumentException.class, () -> ChineseMastery.fromSilkroadId(513));
        assertThrows(IllegalArgumentException.class, () -> ChineseMastery.fromId(257));
    }

    @Test
    void matchesVerifiedNativeChineseStarterRows() {
        Set<Integer> seed = ChineseSkillCatalog.all().stream()
                .filter(ChineseSkillDefinition::isStartingSkill)
                .map(ChineseSkillDefinition::silkroadSkillId)
                .collect(Collectors.toSet());
        assertEquals(Set.of(1, 2, 40, 70), seed);
        assertEquals(ChineseMastery.BICHEON, ChineseSkillCatalog.find(2).orElseThrow().mastery());
        assertEquals(ChineseMastery.HEUKSAL, ChineseSkillCatalog.find(40).orElseThrow().mastery());
        assertEquals(ChineseMastery.PACHEON, ChineseSkillCatalog.find(70).orElseThrow().mastery());
        assertTrue(ChineseSkillCatalog.find(513).isEmpty());
    }

    @Test
    void preservesVerifiedSmashRanksAndCosts() {
        var smashA = ChineseSkillCatalog.find(3).orElseThrow();
        assertEquals(174, smashA.groupId());
        assertEquals(5, smashA.requiredMasteryLevel());
        assertEquals(2, smashA.skillPointCost());

        var smashB = ChineseSkillCatalog.find(4).orElseThrow();
        assertEquals(27, smashB.requiredMasteryLevel());
        assertEquals(117, smashB.skillPointCost());
        assertEquals(174, smashB.prerequisiteGroupId());
        assertEquals(9, smashB.prerequisiteGroupRank());
        assertTrue(smashB.hasPrerequisite());
    }

    @Test
    void cannotPretendTheAionClientHasSkillAnimations() {
        // Id 2 is also present in Aion, but denotes an unrelated template.
        assertFalse(ChineseAionEffectPlan.hasClientVerifiedBinding(2));
        assertFalse(ChineseAionEffectPlan.hasClientVerifiedBinding(40));
        assertTrue(ChineseAionEffectPlan.candidates(ChineseMastery.COLD)
                .contains(EffectType.SLOW));
        assertTrue(ChineseAionEffectPlan.candidates(ChineseMastery.FORCE)
                .contains(EffectType.HEALINSTANT));
    }

    @Test
    void refusesInvalidCatalogueRows() {
        assertThrows(IllegalArgumentException.class, () -> new ChineseSkillDefinition(
                3, "", 174, 1, ChineseMastery.BICHEON, 1, 2, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ChineseSkillDefinition(
                3, "test", 174, 1, ChineseMastery.BICHEON, 1, -1, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new ChineseSkillDefinition(
                3, "test", 174, 1, ChineseMastery.BICHEON, 1, 1, 175, 0, 0));
    }
}
