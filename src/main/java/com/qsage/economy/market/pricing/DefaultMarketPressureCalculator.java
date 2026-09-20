package com.qsage.economy.market.pricing;

public final class DefaultMarketPressureCalculator
        implements MarketPressureCalculator {

    @Override
    public double calculatePressure(
            long buyVolume,
            long sellVolume,
            long expectedVolume
    ) {
        if (expectedVolume <= 0) {
            return 0.0;
        }

        long netVolume = buyVolume - sellVolume;

        double pressure =
                (double) netVolume / expectedVolume;

        return Math.max(
                -1.0,
                Math.min(1.0, pressure)
        );
    }
}