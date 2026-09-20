package com.qsage.config;

public record MarketConfig(
        double priceSpread,
        double trendNeutralThreshold
) {

    public static MarketConfig defaults() {
        return new MarketConfig(
                0.01,
                0.005
        );
    }

    public MarketConfig validate() {
        return new MarketConfig(
                clamp(priceSpread, 0.0, 1.0),
                clamp(trendNeutralThreshold, 0.0, 1.0)
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}