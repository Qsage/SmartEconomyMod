package com.qsage.economy.storage;


import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.TransactionType;
import com.qsage.economy.wallet.Wallet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class SqliteEconomyRepository
        implements EconomyRepository {

    private final EconomyDatabase database;

    public SqliteEconomyRepository(
            EconomyDatabase database
    ) {
        this.database = database;
    }

    // =========================================================
    // READ OPERATIONS
    // =========================================================

    @Override
    public Wallet getWallet(UUID playerId) {

        String sql = """
                SELECT available, locked
                FROM wallets
                WHERE uuid = ?
                """;

        try (PreparedStatement statement =
                     database.connection()
                             .prepareStatement(sql)) {

            statement.setString(
                    1,
                    playerId.toString()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {
                    return Wallet.empty();
                }

                return new Wallet(
                        result.getLong("available"),
                        result.getLong("locked")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load wallet for " + playerId,
                    e
            );
        }
    }

    @Override
    public long getMoneyCreated() {

        String sql = """
                SELECT money_created
                FROM money_supply
                WHERE id = 1
                """;

        try (PreparedStatement statement =
                     database.connection()
                             .prepareStatement(sql);
             ResultSet result =
                     statement.executeQuery()) {

            if (!result.next()) {
                return 0;
            }

            return result.getLong(
                    "money_created"
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load created money",
                    e
            );
        }
    }

    @Override
    public long getMoneyDestroyed() {

        String sql = """
                SELECT money_destroyed
                FROM money_supply
                WHERE id = 1
                """;

        try (PreparedStatement statement =
                     database.connection()
                             .prepareStatement(sql);
             ResultSet result =
                     statement.executeQuery()) {

            if (!result.next()) {
                return 0;
            }

            return result.getLong(
                    "money_destroyed"
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load destroyed money",
                    e
            );
        }
    }

    @Override
    public long calculateWalletSupply() {

        String sql = """
            SELECT COALESCE(
                SUM(available + locked),
                0
            )
            FROM wallets
            """;

        try (PreparedStatement statement =
                     database.connection()
                             .prepareStatement(sql);
             ResultSet result =
                     statement.executeQuery()) {

            if (!result.next()) {
                return 0;
            }

            return result.getLong(1);

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to calculate wallet supply",
                    e
            );
        }
    }

    // =========================================================
    // TRANSACTION
    // =========================================================

    @Override
    public void transaction(
            Consumer<EconomyTransaction> action
    ) {
        Connection connection =
                database.connection();

        boolean previousAutoCommit;

        try {
            previousAutoCommit =
                    connection.getAutoCommit();

            connection.setAutoCommit(false);

            EconomyTransaction transaction =
                    new SqliteEconomyTransaction(
                            connection
                    );

            action.accept(transaction);

            connection.commit();

            connection.setAutoCommit(
                    previousAutoCommit
            );

        } catch (Exception e) {

            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                e.addSuppressed(
                        rollbackException
                );
            }

            try {
                connection.setAutoCommit(true);
            } catch (SQLException autoCommitException) {
                e.addSuppressed(
                        autoCommitException
                );
            }

            throw new RuntimeException(
                    "Economy transaction failed",
                    e
            );
        }
    }

    @Override
    public List<Transaction> getTransactionHistory(
            UUID playerId,
            int limit,
            int offset
    ) {
        String sql = """
            SELECT
                id,
                timestamp,
                type,
                actor_uuid,
                target_uuid,
                amount,
                reason
            FROM transactions
            WHERE actor_uuid = ?
               OR target_uuid = ?
            ORDER BY timestamp DESC, rowid DESC
            LIMIT ? OFFSET ?
            """;

        List<Transaction> history = new ArrayList<>();

        try (PreparedStatement statement =
                     database.connection().prepareStatement(sql)) {

            String playerUuid = playerId.toString();

            statement.setString(1, playerUuid);
            statement.setString(2, playerUuid);
            statement.setInt(3, Math.max(1, Math.min(limit, 100)));
            statement.setInt(4, Math.max(0, offset));

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {

                    Transaction transaction = new Transaction(
                            UUID.fromString(result.getString("id")),
                            Instant.ofEpochMilli(
                                    result.getLong("timestamp")
                            ),
                            TransactionType.valueOf(
                                    result.getString("type")
                            ),
                            readUuid(result.getString("actor_uuid")),
                            readUuid(result.getString("target_uuid")),
                            result.getLong("amount"),
                            result.getString("reason")
                    );

                    history.add(transaction);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load transaction history for " + playerId,
                    e
            );
        }

        return history;
    }

    private static UUID readUuid(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}