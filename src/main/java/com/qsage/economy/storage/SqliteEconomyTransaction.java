package com.qsage.economy.storage;

import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.wallet.Wallet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.UUID;

public final class SqliteEconomyTransaction
        implements EconomyTransaction {

    private final Connection connection;

    public SqliteEconomyTransaction(
            Connection connection
    ) {
        this.connection = connection;
    }

    // =========================================================
    // WALLET
    // =========================================================

    @Override
    public Wallet getWallet(UUID playerId) {

        String sql = """
                SELECT available, locked
                FROM wallets
                WHERE uuid = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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
    public void saveWallet(
            UUID playerId,
            Wallet wallet
    ) {

        long now =
                Instant.now().toEpochMilli();

        String sql = """
                INSERT INTO wallets (
                    uuid,
                    available,
                    locked,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?)
                ON CONFLICT(uuid)
                DO UPDATE SET
                    available = excluded.available,
                    locked = excluded.locked,
                    updated_at = excluded.updated_at
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    playerId.toString()
            );

            statement.setLong(
                    2,
                    wallet.available()
            );

            statement.setLong(
                    3,
                    wallet.locked()
            );

            statement.setLong(
                    4,
                    now
            );

            statement.setLong(
                    5,
                    now
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save wallet for " + playerId,
                    e
            );
        }
    }

    // =========================================================
    // MONEY SUPPLY
    // =========================================================

    @Override
    public long getMoneyCreated() {

        String sql = """
                SELECT money_created
                FROM money_supply
                WHERE id = 1
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);
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
                     connection.prepareStatement(sql);
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
    public void addMoneyCreated(
            long amount
    ) {

        validateAmount(amount);

        String sql = """
                UPDATE money_supply
                SET money_created =
                    money_created + ?
                WHERE id = 1
                """;

        executeAmountUpdate(
                sql,
                amount
        );
    }

    @Override
    public void addMoneyDestroyed(
            long amount
    ) {

        validateAmount(amount);

        String sql = """
                UPDATE money_supply
                SET money_destroyed =
                    money_destroyed + ?
                WHERE id = 1
                """;

        executeAmountUpdate(
                sql,
                amount
        );
    }

    // =========================================================
    // TRANSACTION LEDGER
    // =========================================================

    @Override
    public void saveTransaction(
            Transaction transaction
    ) {

        String sql = """
                INSERT INTO transactions (
                    id,
                    timestamp,
                    type,
                    actor_uuid,
                    target_uuid,
                    amount,
                    reason
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    transaction.id().toString()
            );

            statement.setLong(
                    2,
                    transaction.timestamp()
                            .toEpochMilli()
            );

            statement.setString(
                    3,
                    transaction.type().name()
            );

            if (transaction.actor() != null) {
                statement.setString(
                        4,
                        transaction.actor().toString()
                );
            } else {
                statement.setNull(
                        4,
                        java.sql.Types.VARCHAR
                );
            }

            if (transaction.target() != null) {
                statement.setString(
                        5,
                        transaction.target().toString()
                );
            } else {
                statement.setNull(
                        5,
                        java.sql.Types.VARCHAR
                );
            }

            statement.setLong(
                    6,
                    transaction.amount()
            );

            statement.setString(
                    7,
                    transaction.reason()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save transaction "
                            + transaction.id(),
                    e
            );
        }
    }

    // =========================================================

    private void executeAmountUpdate(
            String sql,
            long amount
    ) {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    amount
            );

            int updated =
                    statement.executeUpdate();

            if (updated != 1) {
                throw new IllegalStateException(
                        "Money supply row was not updated"
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to update money supply",
                    e
            );
        }
    }

    private static void validateAmount(
            long amount
    ) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Amount must be greater than zero"
            );
        }
    }
}