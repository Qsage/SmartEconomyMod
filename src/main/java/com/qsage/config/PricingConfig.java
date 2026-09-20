package com.qsage.config;

public record PricingConfig(
        double smoothingFactor,
        double maxPriceDeviation
) {

    public static PricingConfig defaults() {
        return new PricingConfig(
                0.20,
                0.25
        );
    }

    public PricingConfig validate() {
        return new PricingConfig(
                clamp(smoothingFactor, 0.0, 1.0),
                clamp(maxPriceDeviation, 0.0, 1.0)
        );
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}