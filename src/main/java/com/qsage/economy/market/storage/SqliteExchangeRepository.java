package com.qsage.economy.market.storage;

import com.qsage.economy.market.ExchangeRepository;
import com.qsage.economy.market.model.ExchangeQuote;
import com.qsage.economy.market.model.ExchangeTrade;
import com.qsage.economy.market.model.TradeSide;
import com.qsage.economy.market.model.TradeVolume;
import com.qsage.economy.storage.EconomyDatabase;
import net.minecraft.resources.Identifier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class SqliteExchangeRepository
        implements ExchangeRepository {

    private final Connection connection;

    public SqliteExchangeRepository(
            EconomyDatabase database
    ) {
        this.connection = database.connection();
    }


    /*
     * ============================================================
     * Quotes
     * ============================================================
     */

    @Override
    public ExchangeQuote getQuote(
            Identifier itemId
    ) {
        String sql = """
                SELECT
                    item_id,
                    buy_price,
                    sell_price,
                    available_quantity,
                    timestamp
                FROM exchange_quotes
                WHERE item_id = ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    itemId.toString()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

                if (!result.next()) {
                    return null;
                }

                return new ExchangeQuote(
                        Identifier.parse(
                                result.getString("item_id")
                        ),
                        result.getLong("buy_price"),
                        result.getLong("sell_price"),
                        result.getLong("available_quantity"),
                        result.getLong("timestamp")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load exchange quote: "
                            + itemId,
                    e
            );
        }
    }


    @Override
    public List<ExchangeQuote> getQuotes() {

        String sql = """
                SELECT
                    item_id,
                    buy_price,
                    sell_price,
                    available_quantity,
                    timestamp
                FROM exchange_quotes
                """;

        List<ExchangeQuote> quotes =
                new ArrayList<>();

        try (PreparedStatement statement =
                     connection.prepareStatement(sql);

             ResultSet result =
                     statement.executeQuery()) {

            while (result.next()) {

                quotes.add(
                        new ExchangeQuote(
                                Identifier.parse(
                                        result.getString("item_id")
                                ),
                                result.getLong("buy_price"),
                                result.getLong("sell_price"),
                                result.getLong(
                                        "available_quantity"
                                ),
                                result.getLong("timestamp")
                        )
                );
            }

            return quotes;

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to load exchange quotes",
                    e
            );
        }
    }


    @Override
    public void saveQuote(
            ExchangeQuote quote
    ) {

        String sql = """
                INSERT INTO exchange_quotes (
                    item_id,
                    buy_price,
                    sell_price,
                    available_quantity,
                    timestamp
                )
                VALUES (?, ?, ?, ?, ?)

                ON CONFLICT(item_id)
                DO UPDATE SET
                    buy_price = excluded.buy_price,
                    sell_price = excluded.sell_price,
                    available_quantity = excluded.available_quantity,
                    timestamp = excluded.timestamp
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    quote.itemId().toString()
            );

            statement.setLong(
                    2,
                    quote.buyPrice()
            );

            statement.setLong(
                    3,
                    quote.sellPrice()
            );

            statement.setLong(
                    4,
                    quote.availableQuantity()
            );

            statement.setLong(
                    5,
                    quote.timestamp()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save exchange quote: "
                            + quote.itemId(),
                    e
            );
        }
    }


    /*
     * ============================================================
     * Trade history
     * ============================================================
     */

    @Override
    public void saveTrade(
            ExchangeTrade trade
    ) {

        String sql = """
                INSERT INTO exchange_trades (
                    id,
                    player_uuid,
                    item_id,
                    side,
                    quantity,
                    unit_price,
                    total_price,
                    timestamp
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    trade.id().toString()
            );

            statement.setString(
                    2,
                    trade.playerId().toString()
            );

            statement.setString(
                    3,
                    trade.itemId().toString()
            );

            statement.setString(
                    4,
                    trade.side().name()
            );

            statement.setLong(
                    5,
                    trade.quantity()
            );

            statement.setLong(
                    6,
                    trade.unitPrice()
            );

            statement.setLong(
                    7,
                    trade.totalPrice()
            );

            statement.setLong(
                    8,
                    trade.timestamp().toEpochMilli()
            );

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to save exchange trade: "
                            + trade.id(),
                    e
            );
        }
    }


    /*
     * ============================================================
     * Trade volume
     * ============================================================
     */

    @Override
    public TradeVolume getVolume(
            Identifier itemId,
            Instant from,
            Instant to
    ) {

        String sql = """
                SELECT
                    COALESCE(
                        SUM(
                            CASE
                                WHEN side = 'BUY'
                                THEN quantity
                                ELSE 0
                            END
                        ),
                        0
                    ) AS buy_volume,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN side = 'SELL'
                                THEN quantity
                                ELSE 0
                            END
                        ),
                        0
                    ) AS sell_volume,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN side = 'BUY'
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS buy_trades,

                    COALESCE(
                        SUM(
                            CASE
                                WHEN side = 'SELL'
                                THEN 1
                                ELSE 0
                            END
                        ),
                        0
                    ) AS sell_trades

                FROM exchange_trades

                WHERE item_id = ?
                  AND timestamp >= ?
                  AND timestamp < ?
                """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    itemId.toString()
            );

            statement.setLong(
                    2,
                    from.toEpochMilli()
            );

            statement.setLong(
                    3,
                    to.toEpochMilli()
            );

            try (ResultSet result =
                         statement.executeQuery()) {

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

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Failed to calculate exchange volume: "
                            + itemId,
                    e
            );
        }
    }
}