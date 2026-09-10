package com.qsage.economy.market.storage;

import com.qsage.economy.market.TradeVolumeRepository;
import com.qsage.economy.market.model.TradeVolume;
import net.minecraft.resources.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;

public class SqliteTradeVolumeRepository
        implements TradeVolumeRepository {

    private final Connection connection;

    public SqliteTradeVolumeRepository(Connection connection) {
        this.connection = connection;
    }

    @Override
    public TradeVolume getVolume(
            Identifier itemId,
            Instant from,
            Instant to
    ) {
        String sql = """
                SELECT
                    COALESCE(SUM(
                        CASE
                            WHEN side = 'BUY' THEN quantity
                            ELSE 0
                        END
                    ), 0) AS buy_volume,

                    COALESCE(SUM(
                        CASE
                            WHEN side = 'SELL' THEN quantity
                            ELSE 0
                        END
                    ), 0) AS sell_volume,

                    COALESCE(SUM(
                        CASE
                            WHEN side = 'BUY' THEN 1
                            ELSE 0
                        END
                    ), 0) AS buy_trades,

                    COALESCE(SUM(
                        CASE
                            WHEN side = 'SELL' THEN 1
                            ELSE 0
                        END
                    ), 0) AS sell_trades

                FROM exchange_trades

                WHERE item_id = ?
                  AND timestamp >= ?
                  AND timestamp < ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, itemId.toString());
            statement.setLong(2, from.toEpochMilli());
            statement.setLong(3, to.toEpochMilli());

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return TradeVolume.empty();
                }

                return new TradeVolume(
                        result.getLong("buy_volume"),
                        result.getLong("sell_volume"),
                        result.getLong("buy_trades"),
                        result.getLong("sell_trades")
                );
            }

        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to load exchange trade volume",
                    e
            );
        }
    }
}