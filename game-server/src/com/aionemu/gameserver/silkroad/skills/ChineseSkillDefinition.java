package com.aionemu.gameserver.silkroad.skills;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;

/**
 * Version-specific factual skill metadata, not an Aion client skill template.
 * Keep Silkroad ids separate from Aion ids: id 2 is already an unrelated Aion skill.
 */
public record ChineseSkillDefinition(
        int silkroadSkillId,
        String codename,
        int groupId,
        int rank,
        ChineseMastery mastery,
        int requiredMasteryLevel,
        long skillPointCost,
        int prerequisiteGroupId,
        int prerequisiteGroupRank,
        int chainNextSkillId) {

    public ChineseSkillDefinition {
        if (silkroadSkillId <= 0 || codename == null || codename.isBlank() || groupId <= 0 || rank < 1) {
            throw new IllegalArgumentException("Invalid Silkroad skill identity");
        }
        if (requiredMasteryLevel < 0 || skillPointCost < 0) {
            throw new IllegalArgumentException("Negative skill requirement or SP cost");
        }
        if (mastery == null && requiredMasteryLevel != 0) {
            throw new IllegalArgumentException("Unassigned mastery with required mastery level");
        }
        if (prerequisiteGroupId < 0 || prerequisiteGroupRank < 0 ||
                (prerequisiteGroupId == 0) != (prerequisiteGroupRank == 0)) {
            throw new IllegalArgumentException("Incomplete prerequisite");
        }
        if (chainNextSkillId < 0 || chainNextSkillId == silkroadSkillId) {
            throw new IllegalArgumentException("Invalid chain link");
        }
    }

    public boolean isStartingSkill() {
        return skillPointCost == 0 && rank == 1 && prerequisiteGroupId == 0;
    }

    public boolean hasPrerequisite() {
        return prerequisiteGroupId != 0;
    }
}
