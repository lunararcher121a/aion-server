package com.aionemu.gameserver.silkroad.skills;

import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;
import com.aionemu.gameserver.skillengine.effect.EffectType;

/**
 * Candidate Aion effect primitives for building analogous Silkroad schools.
 * NOT a live SkillTemplate mapping, damage formula, animation mapping, or a
 * proof that Aion implements the identical Silkroad mechanic. In particular
 * multi-hit chains need additional scheduling and client-side animations.
 */
public final class ChineseAionEffectPlan {
    private static final Map<ChineseMastery, List<EffectType>> CANDIDATES = Map.of(
            ChineseMastery.BICHEON, List.of(
                    EffectType.SKILLATTACKINSTANT, EffectType.STUN, EffectType.BLEED),
            ChineseMastery.HEUKSAL, List.of(
                    EffectType.SKILLATTACKINSTANT, EffectType.STAGGER),
            ChineseMastery.PACHEON, List.of(
                    EffectType.SKILLATTACKINSTANT, EffectType.SLOW),
            ChineseMastery.COLD, List.of(
                    EffectType.SPELLATTACKINSTANT, EffectType.SLOW, EffectType.SNARE),
            ChineseMastery.LIGHTNING, List.of(
                    EffectType.SPELLATTACKINSTANT, EffectType.STATUP),
            ChineseMastery.FIRE, List.of(
                    EffectType.SPELLATTACKINSTANT, EffectType.STATUP),
            ChineseMastery.FORCE, List.of(
                    EffectType.HEALINSTANT, EffectType.DISPELDEBUFF, EffectType.STATUP)
    );

    private ChineseAionEffectPlan() {
    }

    public static List<EffectType> candidates(ChineseMastery mastery) {
        return CANDIDATES.getOrDefault(mastery, List.of());
    }

    /**
     * Deliberately no real Aion ids: numeric collisions make automatic
     * binding unsafe until a matching patched client and templates exist.
     */
    public static boolean hasClientVerifiedBinding(int silkroadSkillId) {
        return false;
    }
}
