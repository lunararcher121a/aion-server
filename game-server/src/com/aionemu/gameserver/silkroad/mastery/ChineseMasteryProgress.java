package com.aionemu.gameserver.silkroad.mastery;

import java.util.EnumMap;
import java.util.Map;

/**
 * Pure domain model. Not wired into Aion player lifecycle until packet and persistence
 * integration has been validated. All mutations must occur in an authenticated server
 * transaction and be persisted atomically with the SP balance.
 */
public final class ChineseMasteryProgress {
    private final EnumMap<ChineseMastery, Integer> levels = new EnumMap<>(ChineseMastery.class);
    private long skillPoints;

    public ChineseMasteryProgress(long skillPoints, Map<ChineseMastery, Integer> initialLevels,
            int characterLevel, int totalMasteryCap) {
        if (skillPoints < 0) throw new IllegalArgumentException("Negative SP");
        this.skillPoints = skillPoints;
        for (ChineseMastery mastery : ChineseMastery.values()) {
            int level = initialLevels.getOrDefault(mastery, 0);
            if (level < 0 || level > characterLevel) throw new IllegalArgumentException("Invalid mastery level");
            levels.put(mastery, level);
        }
        if (totalLevel() > totalMasteryCap) throw new IllegalArgumentException("Mastery cap exceeded");
    }

    public synchronized int level(ChineseMastery mastery) { return levels.get(mastery); }
    public synchronized long skillPoints() { return skillPoints; }
    public synchronized int totalLevel() {
        int total = 0;
        for (int level : levels.values()) total += level;
        return total;
    }

    /**
     * Caller supplies server-computed cost for the next level, never client-provided cost.
     * A failed operation leaves the profile unchanged.
     */
    public synchronized void levelUp(ChineseMastery mastery, int characterLevel,
            int totalMasteryCap, long serverCalculatedCost) {
        if (mastery == null || characterLevel < 1 || totalMasteryCap < 0 || serverCalculatedCost < 0)
            throw new IllegalArgumentException("Invalid upgrade parameters");
        int current = level(mastery);
        if (current >= characterLevel) throw new IllegalStateException("Mastery exceeds character level");
        if (totalLevel() >= totalMasteryCap) throw new IllegalStateException("Mastery cap reached");
        if (skillPoints < serverCalculatedCost) throw new IllegalStateException("Insufficient SP");
        levels.put(mastery, current + 1);
        skillPoints -= serverCalculatedCost;
    }
}
