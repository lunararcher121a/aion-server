package com.aionemu.gameserver.silkroad.skills;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.silkroad.mastery.ChineseMastery;

/**
 * Prototype-only transactional store. Does not modify the original Aion
 * players schema, class enums, SkillEngine data, or non-Chinese characters.
 */
public final class BicheonRepository {
    private static final Logger log = LoggerFactory.getLogger(BicheonRepository.class);
    private static final ChineseSkillDefinition SMASH_A =
            ChineseSkillCatalog.find(BicheonPrototype.SILKROAD_SKILL_ID).orElseThrow();
    private static final int REQUIRED_MASTERY = SMASH_A.requiredMasteryLevel();
    private static final long SP_COST = SMASH_A.skillPointCost();
    private static final int LOCAL_BICHEON_ID = ChineseMastery.BICHEON.getId();

    public enum LearnResult {
        LEARNED, NOT_ELIGIBLE, NO_PROFILE, NOT_ENOUGH_MASTERY, NOT_ENOUGH_SP,
        ALREADY_LEARNED, DATABASE_ERROR
    }

    public record State(long skillPoints, int masteryLevel, boolean learned) {
    }

    private BicheonRepository() {
    }

    public static Optional<State> state(int playerId) {
        String sql = """
                SELECT c.skill_points, COALESCE(m.mastery_level, 0) AS mastery_level,
                    EXISTS (SELECT 1 FROM sro_learned_skills s
                        WHERE s.player_id = c.player_id AND s.skill_id = ?) AS learned
                FROM sro_characters c
                LEFT JOIN sro_masteries m ON m.player_id = c.player_id AND m.mastery_id = ?
                WHERE c.player_id = ?
                """;
        try (Connection con = DatabaseFactory.getConnection();
                PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, BicheonPrototype.SILKROAD_SKILL_ID);
            ps.setInt(2, LOCAL_BICHEON_ID);
            ps.setInt(3, playerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next())
                    return Optional.empty();
                return Optional.of(new State(rs.getLong("skill_points"),
                        rs.getInt("mastery_level"), rs.getBoolean("learned")));
            }
        } catch (SQLException e) {
            log.warn("Bicheon state read failed for player {}", playerId, e);
            return Optional.empty();
        }
    }

    /** Read-only authorization predicate. Fail closed on unavailable storage. */
    public static boolean isLearnedWithMastery(int playerId) {
        return state(playerId).filter(s -> s.learned() && s.masteryLevel() >= REQUIRED_MASTERY).isPresent();
    }

    /**
     * Explicit GM-only opt-in. Idempotent; never resets a real profile.
     * No SQL migration is executed automatically.
     */
    public static boolean initializeProfile(int playerId) {
        try (Connection con = DatabaseFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO sro_characters (player_id) VALUES (?)")) {
                    ps.setInt(1, playerId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO sro_masteries (player_id, mastery_id, mastery_level) VALUES (?, ?, 0)")) {
                    for (ChineseMastery mastery : ChineseMastery.values()) {
                        ps.setInt(1, playerId);
                        ps.setInt(2, mastery.getId());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                rollback(con);
                log.warn("Cannot initialize Bicheon test profile {}", playerId, e);
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.warn("Cannot open Bicheon test profile transaction for {}", playerId, e);
            return false;
        }
    }

    /** GM test setup only: no gameplay SP farming has been implemented. */
    public static boolean grantTestSp(int playerId, long amount) {
        if (amount <= 0 || amount > 10000)
            return false;
        try (Connection con = DatabaseFactory.getConnection();
                PreparedStatement ps = con.prepareStatement("""
                        UPDATE sro_characters SET skill_points = skill_points + ?
                        WHERE player_id = ? AND skill_points <= ?""")) {
            ps.setLong(1, amount);
            ps.setInt(2, playerId);
            ps.setLong(3, Long.MAX_VALUE - amount);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            log.warn("Cannot grant test SP to {}", playerId, e);
            return false;
        }
    }

    /** GM test setup only. Keeps the sum of masteries at or below 300. */
    public static boolean setTestBicheonLevel(int playerId, int characterLevel, int newLevel) {
        if (newLevel < 0 || newLevel > characterLevel || characterLevel < 1)
            return false;
        try (Connection con = DatabaseFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Match the purchase lock order: sro_characters first, then
                // sro_masteries. This prevents concurrent GM changes from
                // racing with a normal SP purchase.
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT player_id FROM sro_characters WHERE player_id=? FOR UPDATE")) {
                    ps.setInt(1, playerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            rollback(con);
                            return false;
                        }
                    }
                }
                long total = 0;
                int current = -1;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT mastery_id, mastery_level FROM sro_masteries WHERE player_id = ? FOR UPDATE")) {
                    ps.setInt(1, playerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int masteryId = rs.getInt("mastery_id");
                            int masteryLevel = rs.getInt("mastery_level");
                            total += masteryLevel;
                            if (masteryId == LOCAL_BICHEON_ID)
                                current = masteryLevel;
                        }
                    }
                }
                if (current < 0 || total - current + newLevel > 300) {
                    rollback(con);
                    return false;
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE sro_masteries SET mastery_level=? WHERE player_id=? AND mastery_id=?")) {
                    ps.setInt(1, newLevel);
                    ps.setInt(2, playerId);
                    ps.setInt(3, LOCAL_BICHEON_ID);
                    if (ps.executeUpdate() != 1) {
                        rollback(con);
                        return false;
                    }
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                rollback(con);
                log.warn("Cannot set test Bicheon mastery for {}", playerId, e);
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.warn("Cannot start mastery test transaction for {}", playerId, e);
            return false;
        }
    }

    /**
     * ATOMIC SP PURCHASE: lock profile, check mastery/SP/ownership,
     * debit SP, record Silkroad rank and Aion template id in ONE transaction.
     * A second packet must fail and may never spend SP a second time.
     */
    public static LearnResult learnSmashA(int playerId, int characterLevel) {
        if (characterLevel < REQUIRED_MASTERY || !BicheonPrototype.ENABLED)
            return LearnResult.NOT_ELIGIBLE;
        try (Connection con = DatabaseFactory.getConnection()) {
            con.setAutoCommit(false);
            try {
                Long sp = null;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT skill_points FROM sro_characters WHERE player_id=? FOR UPDATE")) {
                    ps.setInt(1, playerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            sp = rs.getLong(1);
                    }
                }
                if (sp == null) {
                    rollback(con);
                    return LearnResult.NO_PROFILE;
                }
                int masteryLevel = -1;
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT mastery_level FROM sro_masteries WHERE player_id=? AND mastery_id=?")) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, LOCAL_BICHEON_ID);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next())
                            masteryLevel = rs.getInt(1);
                    }
                }
                if (masteryLevel < REQUIRED_MASTERY) {
                    rollback(con);
                    return LearnResult.NOT_ENOUGH_MASTERY;
                }
                if (isExistingSkill(con, playerId)) {
                    rollback(con);
                    return LearnResult.ALREADY_LEARNED;
                }
                if (sp < SP_COST) {
                    rollback(con);
                    return LearnResult.NOT_ENOUGH_SP;
                }

                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE sro_characters SET skill_points=skill_points-? WHERE player_id=? AND skill_points>=?")) {
                    ps.setLong(1, SP_COST);
                    ps.setInt(2, playerId);
                    ps.setLong(3, SP_COST);
                    if (ps.executeUpdate() != 1)
                        throw new SQLException("Atomic SP debit failed");
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO sro_learned_skills (player_id,skill_id,skill_level) VALUES (?,?,1)")) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, BicheonPrototype.SILKROAD_SKILL_ID);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO player_skills (player_id,skill_id,skill_level) VALUES (?,?,1)")) {
                    ps.setInt(1, playerId);
                    ps.setInt(2, BicheonPrototype.AION_SKILL_ID);
                    ps.executeUpdate();
                }
                con.commit();
                return LearnResult.LEARNED;
            } catch (SQLException e) {
                rollback(con);
                log.warn("Atomic Bicheon skill purchase failed for {}", playerId, e);
                return LearnResult.DATABASE_ERROR;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            log.warn("Cannot start atomic Bicheon purchase for {}", playerId, e);
            return LearnResult.DATABASE_ERROR;
        }
    }

    private static boolean isExistingSkill(Connection con, int playerId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("""
                SELECT 1 FROM sro_learned_skills WHERE player_id=? AND skill_id=?
                UNION ALL
                SELECT 1 FROM player_skills WHERE player_id=? AND skill_id=? LIMIT 1""")) {
            ps.setInt(1, playerId);
            ps.setInt(2, BicheonPrototype.SILKROAD_SKILL_ID);
            ps.setInt(3, playerId);
            ps.setInt(4, BicheonPrototype.AION_SKILL_ID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException rollbackError) {
            log.error("Could not roll back Bicheon transaction", rollbackError);
        }
    }
}
