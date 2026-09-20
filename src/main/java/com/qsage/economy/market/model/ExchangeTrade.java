package com.qsage.economy.market.model;

import net.minecraft.resources.Identifier;

import java.time.Instant;
import java.util.UUID;

public record ExchangeTrade(
        UUID id,
        UUID playerId,
        Identifier itemId,
        TradeSide side,
        long quantity,
        long unitPrice,
        long totalPrice,
        Instant timestamp
) {

    public ExchangeTrade {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }

        if (unitPrice <= 0) {
            throw new IllegalArgumentException(
                    "Unit price must be positive"
            );
        }

        if (totalPrice <= 0) {
            throw new IllegalArgumentException(
                    "Total price must be positive"
            );
        }
    }
}