package com.qsage.economy.market.model;

import net.minecraft.resources.Identifier;

public record ExchangeQuote(
        Identifier itemId,
        long buyPrice,
        long sellPrice,
        long availableQuantity,
        long timestamp
) {

    public ExchangeQuote {
        if (buyPrice <= 0) {
            throw new IllegalArgumentException(
                    "Buy price must be positive"
            );
        }

        if (sellPrice <= 0) {
            throw new IllegalArgumentException(
                    "Sell price must be positive"
            );
        }

        if (sellPrice > buyPrice) {
            throw new IllegalArgumentException(
                    "Sell price cannot be greater than buy price"
            );
        }

        if (availableQuantity < 0) {
            throw new IllegalArgumentException(
                    "Available quantity cannot be negative"
            );
        }
    }
}