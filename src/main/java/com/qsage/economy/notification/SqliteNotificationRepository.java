
package com.qsage.economy.notification;

import com.qsage.economy.storage.EconomyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SqliteNotificationRepository {

    private final Connection connection;

    public SqliteNotificationRepository(EconomyDatabase database) {
        this.connection = database.connection();
    }

    public void addPendingNotification(
            UUID playerUuid,
            UUID senderUuid,
            long amount
    ) {
        if (playerUuid == null || senderUuid == null) {
            throw new IllegalArgumentException("Player UUID cannot be null");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        String sql = """
                INSERT INTO pending_notifications (
                    player_uuid,
                    sender_uuid,
                    amount,
                    created_at,
                    delivered
                )
                VALUES (?, ?, ?, ?, 0)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerUuid.toString());
            statement.setString(2, senderUuid.toString());
            statement.setLong(3, amount);
            statement.setLong(4, System.currentTimeMillis());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save pending notification", e
            );
        }
    }

    public List<PendingNotification> getPendingNotifications(
            UUID playerUuid
    ) {
        String sql = """
                SELECT
                    id,
                    player_uuid,
                    sender_uuid,
                    amount,
                    created_at
                FROM pending_notifications
                WHERE player_uuid = ?
                  AND delivered = 0
                ORDER BY created_at ASC, id ASC
                """;

        List<PendingNotification> notifications =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerUuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    notifications.add(
                            new PendingNotification(
                                    result.getLong("id"),
                                    UUID.fromString(
                                            result.getString("player_uuid")
                                    ),
                                    UUID.fromString(
                                            result.getString("sender_uuid")
                                    ),
                                    result.getLong("amount"),
                                    result.getLong("created_at")
                            )
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load pending notifications", e
            );
        }

        return notifications;
    }

    public void markNotificationsDelivered(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        String sql = """
                UPDATE pending_notifications
                SET delivered = 1
                WHERE id = ?
                  AND delivered = 0
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            for (Long id : ids) {
                statement.setLong(1, id);
                statement.addBatch();
            }

            statement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to mark notifications delivered", e
            );
        }
    }


    public void markDeliveredUpTo(
            UUID playerUuid,
            long lastNotificationId
    ) {
        if (playerUuid == null || lastNotificationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid notification acknowledgement"
            );
        }

        String sql = """
            UPDATE pending_notifications
            SET delivered = 1
            WHERE player_uuid = ?
              AND id <= ?
              AND delivered = 0
            """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerUuid.toString());
            statement.setLong(2, lastNotificationId);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to acknowledge offline notifications",
                    e
            );
        }
    }


    public PendingSummary getPendingSummary(UUID playerUuid) {
        String sql = """
                SELECT
                    COUNT(*) AS transfer_count,
                    COALESCE(SUM(amount), 0) AS total_amount
                FROM pending_notifications
                WHERE player_uuid = ?
                  AND delivered = 0
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerUuid.toString());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return new PendingSummary(
                            result.getLong("transfer_count"),
                            result.getLong("total_amount")
                    );
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load pending summary", e
            );
        }

        return new PendingSummary(0, 0);
    }

    public record PendingSummary(
            long transferCount,
            long totalAmount
    ) {
    }
}
