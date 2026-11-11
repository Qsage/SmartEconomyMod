package com.qsage.economy.storage;

import com.qsage.economy.bank.BankFavoriteRepository;
import com.qsage.economy.storage.EconomyDatabase;

import java.sql.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class SqliteBankFavoriteRepository
        implements BankFavoriteRepository {

    private final Connection connection;

    public SqliteBankFavoriteRepository(EconomyDatabase database) {
        this.connection = database.connection();
    }

    @Override
    public Set<UUID> getFavorites(UUID owner) {
        String sql = """
            SELECT target_uuid
            FROM bank_favorites
            WHERE owner_uuid = ?
            """;

        Set<UUID> favorites = new HashSet<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, owner.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    favorites.add(UUID.fromString(
                            result.getString("target_uuid")
                    ));
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load bank favorites", e
            );
        }

        return favorites;
    }

    @Override
    public boolean isFavorite(UUID owner, UUID target) {
        String sql = """
            SELECT 1
            FROM bank_favorites
            WHERE owner_uuid = ?
              AND target_uuid = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, owner.toString());
            statement.setString(2, target.toString());

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to check bank favorite", e
            );
        }
    }

    @Override
    public void addFavorite(UUID owner, UUID target) {
        String sql = """
            INSERT OR IGNORE INTO bank_favorites (
                owner_uuid,
                target_uuid,
                created_at
            )
            VALUES (?, ?, ?)
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, owner.toString());
            statement.setString(2, target.toString());
            statement.setLong(
                    3,
                    Instant.now().getEpochSecond()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to add bank favorite", e
            );
        }
    }

    @Override
    public void removeFavorite(UUID owner, UUID target) {
        String sql = """
            DELETE FROM bank_favorites
            WHERE owner_uuid = ?
              AND target_uuid = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, owner.toString());
            statement.setString(2, target.toString());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to remove bank favorite", e
            );
        }
    }
}

