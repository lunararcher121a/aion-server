package com.aionemu.gameserver.silkroad.skills;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;

/**
 * Initial, explicitly verified subset of Silkroad Legend III v1.150 skill data.
 * Source: opensro/internal/game/enterworld/defaultskills_test.go and
 * opensro/internal/game/progression/skilllearn_test.go.
 *
 * These numeric ids are ONLY valid for that Silkroad version. They are not
 * Aion SkillTemplate ids. Future imports must resolve codenames against the
 * target Silkroad data files and refuse missing or ambiguous skill rows.
 */
public final class ChineseSkillCatalog {
    private static final Map<Integer, ChineseSkillDefinition> BY_ID = build();

    private ChineseSkillCatalog() {
    }

    private static Map<Integer, ChineseSkillDefinition> build() {
        Map<Integer, ChineseSkillDefinition> skills = new LinkedHashMap<>();
        add(skills, new ChineseSkillDefinition(
                1, "SKILL_PUNCH_01", 172, 1, null, 0, 0, 0, 0, 0));
        add(skills, new ChineseSkillDefinition(
                2, "SKILL_CH_SWORD_BASE_01", 173, 1, ChineseMastery.BICHEON, 0, 0, 0, 0, 0));
        add(skills, new ChineseSkillDefinition(
                3, "SKILL_CH_SWORD_SMASH_A_01", 174, 1, ChineseMastery.BICHEON, 5, 2, 0, 0, 0));
        add(skills, new ChineseSkillDefinition(
                4, "SKILL_CH_SWORD_SMASH_B_01", 175, 1, ChineseMastery.BICHEON, 27, 117, 174, 9, 0));
        // OpenSRO verifies a sword chain root at id 6 (group 177; 7 mastery;
        // 5 SP; links to sub-stage 7 then 8), but the exact codename is not
        // confirmed in the committed source. Do not guess a version key.
        // Add it only when v1.150 skilldata can be checked by codename.
        add(skills, new ChineseSkillDefinition(
                40, "SKILL_CH_SPEAR_BASE_01", 195, 1, ChineseMastery.HEUKSAL, 0, 0, 0, 0, 0));
        add(skills, new ChineseSkillDefinition(
                70, "SKILL_CH_BOW_BASE_01", 217, 1, ChineseMastery.PACHEON, 0, 0, 0, 0, 0));
        return Map.copyOf(skills);
    }

    private static void add(Map<Integer, ChineseSkillDefinition> skills, ChineseSkillDefinition skill) {
        if (skills.putIfAbsent(skill.silkroadSkillId(), skill) != null) {
            throw new IllegalArgumentException("Duplicate Silkroad skill id " + skill.silkroadSkillId());
        }
    }

    public static Optional<ChineseSkillDefinition> find(int silkroadSkillId) {
        return Optional.ofNullable(BY_ID.get(silkroadSkillId));
    }

    public static Collection<ChineseSkillDefinition> all() {
        return BY_ID.values();
    }
}
