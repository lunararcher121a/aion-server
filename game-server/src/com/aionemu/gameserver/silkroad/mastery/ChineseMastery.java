package com.aionemu.gameserver.silkroad.mastery;

/**
 * The database uses stable local ids (1..7). Silkroad v1.150 uses separate
 * mastery ids (257..276); never confuse them with Aion skill template ids.
 *
 * Source of v1.150 identifiers:
 * opensro/apps/server/internal/domain/progression.go (chMasteryIDs).
 */
public enum ChineseMastery {
    BICHEON(1, 257),
    HEUKSAL(2, 258),
    PACHEON(3, 259),
    COLD(4, 273),
    LIGHTNING(5, 274),
    FIRE(6, 275),
    FORCE(7, 276);

    private final int id;
    private final int silkroadId;

    ChineseMastery(int id, int silkroadId) {
        this.id = id;
        this.silkroadId = silkroadId;
    }

    /** Existing local database key; unchanged for migration compatibility. */
    public int getId() {
        return id;
    }

    /** Native Silkroad v1.150 mastery identifier. */
    public int getSilkroadId() {
        return silkroadId;
    }

    public static ChineseMastery fromId(int id) {
        for (ChineseMastery mastery : values()) {
            if (mastery.id == id) {
                return mastery;
            }
        }
        throw new IllegalArgumentException("Unknown local Chinese mastery id: " + id);
    }

    public static ChineseMastery fromSilkroadId(int silkroadId) {
        for (ChineseMastery mastery : values()) {
            if (mastery.silkroadId == silkroadId) {
                return mastery;
            }
        }
        throw new IllegalArgumentException("Unknown Silkroad Chinese mastery id: " + silkroadId);
    }
}
