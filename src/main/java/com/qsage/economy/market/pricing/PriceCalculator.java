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
}