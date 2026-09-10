package com.qsage.economy.market.model;

import net.minecraft.resources.Identifier;

public record ExchangeAsset(
        Identifier itemId,
        long basePrice,
        long valuePpm
) {

    public static final long VALUE_SCALE = 1_000_000L;

    public ExchangeAsset {
        if (basePrice <= 0) {
            throw new IllegalArgumentException(
                    "Base price must be positive"
            );
        }

        if (valuePpm <= 0) {
            throw new IllegalArgumentException(
                    "Value must be positive"
            );
        }
    }

    public long fundamentalPrice() {
        return Math.multiplyExact(basePrice, valuePpm)
                / VALUE_SCALE;
    }
}