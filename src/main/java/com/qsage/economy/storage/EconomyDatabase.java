package com.qsage.economy.storage;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class EconomyDatabase implements AutoCloseable {

    private final Path databasePath;
    private final Connection connection;

    public EconomyDatabase(MinecraftServer server) {
        try {
            Path worldPath =
                    server.getWorldPath(LevelResource.ROOT);

            Path economyDirectory =
                    worldPath.resolve("economy");

            Files.createDirectories(economyDirectory);

            this.databasePath =
                    economyDirectory.resolve("economy.db");

            this.connection =
                    DriverManager.getConnection(
                            "jdbc:sqlite:" + databasePath
                    );

            configure();
            createSchema();

        } catch (IOException | SQLException e) {
            throw new RuntimeException(
                    "Failed to initialize economy database",
                    e
            );
        }
    }

    private void configure() throws SQLException {
        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
            statement.execute("PRAGMA busy_timeout = 5000");
        }
    }

    private void createSchema() throws SQLException {
        try (Statement statement =
                     connection.createStatement()) {

            /*
             * ============================================================
             * Economy
             * ============================================================
             */

            statement.execute("""
                CREATE TABLE IF NOT EXISTS wallets (
                    uuid TEXT PRIMARY KEY,
                    available INTEGER NOT NULL,
                    locked INTEGER NOT NULL,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS money_supply (
                    id INTEGER PRIMARY KEY CHECK (id = 1),
                    money_created INTEGER NOT NULL,
                    money_destroyed INTEGER NOT NULL
                )
                """);

            statement.execute("""
                INSERT OR IGNORE INTO money_supply (
                    id,
                    money_created,
                    money_destroyed
                )
                VALUES (1, 0, 0)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id TEXT PRIMARY KEY,
                    timestamp INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    actor_uuid TEXT,
                    target_uuid TEXT,
                    amount INTEGER NOT NULL,
                    reason TEXT
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_transactions_actor
                ON transactions(actor_uuid)
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_transactions_target
                ON transactions(target_uuid)
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_transactions_timestamp
                ON transactions(timestamp)
                """);


            /*
             * ============================================================
             * Exchange
             * ============================================================
             *
             * Текущая котировка каждого биржевого предмета.
             */

            statement.execute("""
                CREATE TABLE IF NOT EXISTS exchange_quotes (
                    item_id TEXT PRIMARY KEY,
                    buy_price INTEGER NOT NULL,
                    sell_price INTEGER NOT NULL,
                    available_quantity INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL
                )
                """);


            /*
             * ============================================================
             * Exchange trade history
             * ============================================================
             */

            statement.execute("""
                CREATE TABLE IF NOT EXISTS exchange_trades (
                    id TEXT PRIMARY KEY,
                    player_uuid TEXT NOT NULL,
                    item_id TEXT NOT NULL,
                    side TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    unit_price INTEGER NOT NULL,
                    total_price INTEGER NOT NULL,
                    timestamp INTEGER NOT NULL
                )
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_exchange_trades_item_timestamp
                ON exchange_trades(item_id, timestamp)
                """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS
                idx_exchange_trades_timestamp
                ON exchange_trades(timestamp)
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS known_players (
                    uuid TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    first_seen INTEGER NOT NULL,
                    last_seen INTEGER NOT NULL,
                    profile_data TEXT
                );
            """);

            statement.execute("""
                CREATE INDEX IF NOT EXISTS idx_known_players_name
                ON known_players(name COLLATE NOCASE);
            """);
        }
    }

    public Connection connection() {
        return connection;
    }

    public Path databasePath() {
        return databasePath;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to close economy database",
                    e
            );
        }
    }
}