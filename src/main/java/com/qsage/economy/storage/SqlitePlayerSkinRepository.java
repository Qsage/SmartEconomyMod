
package com.qsage.economy.storage;

import com.qsage.economy.player.skin.PlayerSkinRepository;
import com.qsage.economy.player.skin.StoredPlayerSkin;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class SqlitePlayerSkinRepository
        implements PlayerSkinRepository {

    private final Connection connection;

    public SqlitePlayerSkinRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(StoredPlayerSkin skin) {
        String sql = """
            INSERT INTO player_skins (
                player_uuid,
                texture_value,
                texture_signature,
                updated_at
            )
            VALUES (?, ?, ?, ?)
            ON CONFLICT(player_uuid) DO UPDATE SET
                texture_value = excluded.texture_value,
                texture_signature = excluded.texture_signature,
                updated_at = excluded.updated_at
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    skin.playerUuid().toString()
            );

            statement.setString(
                    2,
                    skin.textureValue()
            );

            if (skin.textureSignature() != null) {
                statement.setString(
                        3,
                        skin.textureSignature()
                );
            } else {
                statement.setNull(3, Types.VARCHAR);
            }

            statement.setLong(
                    4,
                    skin.updatedAt()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save player skin "
                            + skin.playerUuid(),
                    e
            );
        }
    }

    @Override
    public Optional<StoredPlayerSkin> findByUuid(UUID uuid) {
        String sql = """
            SELECT
                player_uuid,
                texture_value,
                texture_signature,
                updated_at
            FROM player_skins
            WHERE player_uuid = ?
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, uuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(readSkin(result));
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load player skin " + uuid,
                    e
            );
        }
    }

    @Override
    public List<StoredPlayerSkin> getAll() {
        String sql = """
            SELECT
                player_uuid,
                texture_value,
                texture_signature,
                updated_at
            FROM player_skins
            ORDER BY updated_at DESC
            """;

        List<StoredPlayerSkin> skins = new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                skins.add(readSkin(result));
            }

            return skins;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load player skins",
                    e
            );
        }
    }

    private StoredPlayerSkin readSkin(ResultSet result)
            throws SQLException {

        return new StoredPlayerSkin(
                UUID.fromString(
                        result.getString("player_uuid")
                ),
                result.getString("texture_value"),
                result.getString("texture_signature"),
                result.getLong("updated_at")
        );
    }
}
