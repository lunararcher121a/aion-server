package com.aionemu.gameserver.silkroad.skills;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

import com.aionemu.gameserver.silkroad.mastery.ChineseMasteryProgress;

/**
 * Pure preflight for TRAINING a Silkroad skill (not for casting it).
 * Authoritative callers must reload/lock the player row and re-check these
 * predicates in the SAME transaction as deducting SP and saving the skill.
 * Character client requests must never supply the SP cost or prerequisites.
 */
public final class ChineseSkillEligibility {
    public enum Result {
        AVAILABLE,
        ALREADY_LEARNED,
        MASTERY_TOO_LOW,
        PREREQUISITE_MISSING,
        INSUFFICIENT_SP
    }

    private ChineseSkillEligibility() {
    }

    /**
     * learnedGroupRank provides the highest persisted rank for the skill
     * group; absence is represented as 0. The caller owns this trusted state.
     */
    public static Result check(ChineseSkillDefinition skill,
            ChineseMasteryProgress progress, IntUnaryOperator learnedGroupRank) {
        Objects.requireNonNull(skill, "skill");
        Objects.requireNonNull(progress, "progress");
        Objects.requireNonNull(learnedGroupRank, "learnedGroupRank");

        if (learnedGroupRank.applyAsInt(skill.groupId()) >= skill.rank()) {
            return Result.ALREADY_LEARNED;
        }
        if (skill.mastery() != null &&
                progress.level(skill.mastery()) < skill.requiredMasteryLevel()) {
            return Result.MASTERY_TOO_LOW;
        }
        if (skill.hasPrerequisite() &&
                learnedGroupRank.applyAsInt(skill.prerequisiteGroupId()) < skill.prerequisiteGroupRank()) {
            return Result.PREREQUISITE_MISSING;
        }
        if (progress.skillPoints() < skill.skillPointCost()) {
            return Result.INSUFFICIENT_SP;
        }
        return Result.AVAILABLE;
    }
}
