package com.qsage.economy.market.pricing;

public final class LinearPriceSmoother
        implements PriceSmoother {

    private final double factor;

    public LinearPriceSmoother(double factor) {
        if (factor <= 0.0 || factor > 1.0) {
            throw new IllegalArgumentException(
                    "factor must be > 0 and <= 1"
            );
        }

        this.factor = factor;
    }

    @Override
    public long smooth(
            long currentPrice,
            long targetPrice
    ) {
        if (currentPrice == targetPrice) {
            return currentPrice;
        }

        double result =
                currentPrice
                        + (targetPrice - currentPrice)
                        * factor;

        return Math.max(
                1L,
                Math.round(result)
        );
    }
}