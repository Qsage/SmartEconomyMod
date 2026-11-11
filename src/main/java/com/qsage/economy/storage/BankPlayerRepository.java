
package com.qsage.economy.storage;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class BankPlayerRepository {

    public record Entry(
            UUID uuid,
            String name,
            boolean favorite
    ) {}

    private final Connection connection;

    public BankPlayerRepository(EconomyDatabase database) {
        this.connection = database.connection();
    }

    public void rememberPlayer(UUID uuid, String name) {
        long now = Instant.now().toEpochMilli();

        try (PreparedStatement ps = connection.prepareStatement("""
                INSERT INTO known_players
                    (uuid, name, first_seen, last_seen)
                VALUES (?, ?, ?, ?)
                ON CONFLICT(uuid) DO UPDATE SET
                    name = excluded.name,
                    last_seen = excluded.last_seen
                """)) {

            ps.setString(1, uuid.toString());
            ps.setString(2, name);
            ps.setLong(3, now);
            ps.setLong(4, now);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to remember player", e);
        }
    }

    public List<Entry> getPlayers(UUID viewer) {
        List<Entry> result = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement("""
                SELECT p.uuid, p.name,
                       CASE WHEN f.target_uuid IS NULL
                            THEN 0 ELSE 1 END AS favorite
                FROM known_players p
                LEFT JOIN bank_favorites f
                    ON f.target_uuid = p.uuid
                   AND f.owner_uuid = ?
                WHERE p.uuid <> ?
                ORDER BY p.name COLLATE NOCASE
                """)) {

            ps.setString(1, viewer.toString());
            ps.setString(2, viewer.toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Entry(
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("name"),
                            rs.getInt("favorite") != 0
                    ));
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to load bank players", e);
        }

        return result;
    }

    public void setFavorite(
            UUID owner,
            UUID target,
            boolean favorite
    ) {
        if (owner.equals(target)) {
            throw new IllegalArgumentException(
                    "Cannot favorite yourself");
        }

        String sql = favorite
                ? """
                  INSERT OR IGNORE INTO bank_favorites
                      (owner_uuid, target_uuid, created_at)
                  VALUES (?, ?, ?)
                  """
                : """
                  DELETE FROM bank_favorites
                  WHERE owner_uuid = ? AND target_uuid = ?
                  """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, owner.toString());
            ps.setString(2, target.toString());

            if (favorite) {
                ps.setLong(3, Instant.now().toEpochMilli());
            }

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Failed to update favorite", e);
        }
    }
}
