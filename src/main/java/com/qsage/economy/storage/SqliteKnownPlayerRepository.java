package com.qsage.economy.storage;

import com.qsage.economy.player.KnownPlayer;
import com.qsage.economy.player.KnownPlayerRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SqliteKnownPlayerRepository
        implements KnownPlayerRepository {

    private final Connection connection;

    public SqliteKnownPlayerRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saverOrUpdate(KnownPlayer player) {
        String sql = """
            INSERT INTO known_players (
                uuid,
                name,
                first_seen,
                last_seen,
                profile_data
            )
            VALUES (?, ?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET
                name = excluded.name,
                last_seen = excluded.last_seen,
                profile_data = excluded.profile_data
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, player.uuid().toString());
            statement.setString(2, player.name());
            statement.setLong(3, player.firstSeen());
            statement.setLong(4, player.lastSeen());

            if (player.profileData() != null) {
                statement.setString(5, player.profileData());
            } else {
                statement.setNull(5, Types.VARCHAR);
            }

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save known player " + player.uuid(),
                    e
            );
        }
    }

    @Override
    public KnownPlayer get(UUID uuid) {
        String sql = """
                SELECT
                    uuid,
                    name,
                    first_seen,
                    last_seen,
                    profile_data
                FROM known_players
                WHERE uuid = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return readPlayer(result);
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load known player " + uuid,
                    e
            );
        }
    }

    @Override
    public List<KnownPlayer> getAll() {
        String sql = """
                SELECT
                    uuid,
                    name,
                    first_seen,
                    last_seen,
                    profile_data
                FROM known_players
                ORDER BY name COLLATE NOCASE ASC
                """;

        List<KnownPlayer> players = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                players.add(readPlayer(result));
            }

            return players;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load known players",
                    e
            );
        }
    }

    @Override
    public List<KnownPlayer> searchByName(String name) {
        String sql = """
                SELECT
                    uuid,
                    name,
                    first_seen,
                    last_seen,
                    profile_data
                FROM known_players
                WHERE name LIKE ? COLLATE NOCASE
                ORDER BY name COLLATE NOCASE ASC
                """;

        List<KnownPlayer> players = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, "%" + name + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    players.add(readPlayer(result));
                }
            }

            return players;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to search known players: " + name,
                    e
            );
        }
    }

    private KnownPlayer readPlayer(ResultSet result)
            throws SQLException {

        UUID uuid = UUID.fromString(
                result.getString("uuid")
        );

        String name = result.getString("name");

        long firstSeen =
                result.getLong("first_seen");

        long lastSeen =
                result.getLong("last_seen");

        String profileData =
                result.getString("profile_data");

        return new KnownPlayer(
                uuid,
                name,
                firstSeen,
                lastSeen,
                profileData
        );
    }
}