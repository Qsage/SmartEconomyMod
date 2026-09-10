package com.qsage.economy.market.pricing;

public interface MarketPressureCalculator {

    double calculatePressure(
            long buyVolume,
            long sellVolume,
            long expectedVolume
    );
}
