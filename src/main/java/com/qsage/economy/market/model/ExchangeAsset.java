package com.qsage.economy.market.model;

import net.minecraft.resources.Identifier;

public record ExchangeAsset(
        Identifier itemId,
        long basePrice,
        long valuePpm,
        long expectedVolume,
        ExchangeCategory category
) {
    public static final long VALUE_SCALE = 1_000_000L;

    public ExchangeAsset {
        if (basePrice <= 0) {
            throw new IllegalArgumentException(
                    "basePrice must be positive"
            );
        }

        if (valuePpm <= 0) {
            throw new IllegalArgumentException(
                    "valuePpm must be positive"
            );
        }

        if (expectedVolume <= 0) {
            throw new IllegalArgumentException(
                    "expectedVolume must be positive"
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "category cannot be null"
            );
        }
    }

    public long fundamentalPrice() {
        return Math.multiplyExact(
                basePrice,
                valuePpm
        ) / VALUE_SCALE;
    }
}