package com.qsage.economy.storage;


import com.qsage.economy.wallet.Wallet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}