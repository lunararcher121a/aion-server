package com.aionemu.gameserver.silkroad.combat;

import java.util.Objects;
import java.util.function.IntUnaryOperator;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;
import com.aionemu.gameserver.silkroad.mastery.ChineseMasteryProgress;
import com.aionemu.gameserver.silkroad.skills.ChineseSkillCatalog;
import com.aionemu.gameserver.silkroad.skills.ChineseSkillDefinition;
import com.aionemu.gameserver.silkroad.skills.ChineseSkillEligibility;

/**
 * Server-side prototype of v1.150 Bicheon Smash A (Silkroad skill id 3).
 *
 * No arbitrary Aion skill ids, client animations, DB writes or damage to live
 * creatures occur here. The Aion combat pipeline must remain authoritative
 * for hit, defence, resist, range, PvP and death handling.
 */
public final class BicheonSmashPrototype {
    public static final int SILKROAD_SKILL_ID = 3;
    public static final int REQUIRED_MASTERY = 5;
    public static final long TRAINING_SP = 2;

    private BicheonSmashPrototype() {
    }

    public enum CastStatus {
        READY, NOT_LEARNED, WRONG_WEAPON, INVALID_TARGET, MASTERY_TOO_LOW,
        NOT_ENOUGH_MP, ON_COOLDOWN
    }

    /** Pure cast-time eligibility, independent from one-time SP training cost. */
    public static CastStatus canCast(boolean learned, boolean swordOrBladeEquipped,
            boolean validHostileTarget, int bicheonLevel, int currentMp, int mpCost,
            boolean cooldownReady) {
        if (!learned) return CastStatus.NOT_LEARNED;
        if (!swordOrBladeEquipped) return CastStatus.WRONG_WEAPON;
        if (!validHostileTarget) return CastStatus.INVALID_TARGET;
        if (bicheonLevel < REQUIRED_MASTERY) return CastStatus.MASTERY_TOO_LOW;
        if (mpCost < 0 || currentMp < mpCost) return CastStatus.NOT_ENOUGH_MP;
        if (!cooldownReady) return CastStatus.ON_COOLDOWN;
        return CastStatus.READY;
    }

    /** Uses verified OpenSRO skill metadata; SP is charged only when learning. */
    public static ChineseSkillEligibility.Result canLearn(
            ChineseMasteryProgress profile, IntUnaryOperator learnedGroupRank) {
        Objects.requireNonNull(profile, "profile");
        ChineseSkillDefinition skill = ChineseSkillCatalog.find(SILKROAD_SKILL_ID)
                .orElseThrow(() -> new IllegalStateException("Missing Bicheon Smash A metadata"));
        if (skill.mastery() != ChineseMastery.BICHEON ||
                skill.requiredMasteryLevel() != REQUIRED_MASTERY ||
                skill.skillPointCost() != TRAINING_SP) {
            throw new IllegalStateException("Bicheon Smash A metadata mismatch");
        }
        return ChineseSkillEligibility.check(skill, profile, learnedGroupRank);
    }

    /**
     * Prototype flat bonus for server-side tuning; not the original Silkroad
     * damage formula. This is a bonus input for Aion's real damage pipeline,
     * NOT direct subtraction from a creature's HP.
     */
    public static int calculatePrototypeBonus(int physicalAttack, int skillRank) {
        if (physicalAttack < 0 || skillRank < 1) {
            throw new IllegalArgumentException("Invalid attack or skill rank");
        }
        long bonus = (long) physicalAttack * (10L + Math.min(skillRank, 20)) / 100L;
        return (int) Math.min(bonus, Integer.MAX_VALUE);
    }
}
