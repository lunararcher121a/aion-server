package com.aionemu.gameserver.silkroad.mastery;

/** Server-side Chinese mastery identifiers. No client protocol IDs are changed. */
public enum ChineseMastery {
    BICHEON(1), HEUKSAL(2), PACHEON(3), COLD(4), LIGHTNING(5), FIRE(6), FORCE(7);

    private final int id;
    ChineseMastery(int id) { this.id = id; }
    public int getId() { return id; }

    public static ChineseMastery fromId(int id) {
        for (ChineseMastery mastery : values())
            if (mastery.id == id) return mastery;
        throw new IllegalArgumentException("Unknown Chinese mastery id: " + id);
    }
}
