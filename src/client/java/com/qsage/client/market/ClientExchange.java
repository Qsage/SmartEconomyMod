package com.qsage.client.market;

import com.qsage.economy.market.network.ExchangeQuotePayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClientExchange {

    private static final Map<
            Identifier,
            ExchangeQuotePayload
            > quotes = new HashMap<>();

    private ClientExchange() {
    }

    public static void update(
            ExchangeSnapshotPayload payload
    ) {
        quotes.clear();

        for (ExchangeQuotePayload quote : payload.quotes()) {
            quotes.put(
                    quote.itemId(),
                    quote
            );
        }
    }

    public static ExchangeQuotePayload get(
            Identifier itemId
    ) {
        return quotes.get(itemId);
    }

    public static Map<
            Identifier,
            ExchangeQuotePayload
            > getAll() {
        return Collections.unmodifiableMap(quotes);
    }

    public static void clear() {
        quotes.clear();
    }
}