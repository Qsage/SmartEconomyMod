package com.qsage.economy.market.pricing;

import com.qsage.economy.market.model.ExchangeAsset;

public final class PriceCalculator {

    private PriceCalculator() {
    }

    public static long calculateFundamentalPrice(
            ExchangeAsset asset
    ) {
        return asset.fundamentalPrice();
    }

    public long clampToFundamentalRange(
            long fundamentalPrice,
            long targetPrice,
            double maxDeviation
    ) {
        double minPrice =
                fundamentalPrice * (1.0 - maxDeviation);

        double maxPrice =
                fundamentalPrice * (1.0 + maxDeviation);

        return Math.round(
                Math.max(
                        minPrice,
                        Math.min(maxPrice, targetPrice)
                )
        );
    }
}