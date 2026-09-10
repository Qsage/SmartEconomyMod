package com.qsage.economy.market.pricing;

public interface PriceSmoother {

    long smooth(
            long currentPrice,
            long targetPrice
    );
}
