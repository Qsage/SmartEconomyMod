package com.qsage.config;

public record InflationConfig(
        double targetInflation,
        double kp,
        double ki,
        double kd,
        double maxCorrection,
        long evaluationPeriod
) {

    public static InflationConfig defaults() {
        return new InflationConfig(
                0.02,   // 2% целевая инфляция
                0.50,   // Kp
                0.05,   // Ki
                0.10,   // Kd
                0.25,   // максимум коррекции 25%
                3600    // 1 час
        );
    }

    public InflationConfig validate() {
        return new InflationConfig(
                clamp(targetInflation, -1.0, 1.0),
                clamp(kp, 0.0, 10.0),
                clamp(ki, 0.0, 10.0),
                clamp(kd, 0.0, 10.0),
                clamp(maxCorrection, 0.0, 1.0),
                Math.max(60, evaluationPeriod)
        );
    }

    private static double clamp(
            double value,
            double min,
            double max
    ) {
        return Math.max(min, Math.min(max, value));
    }
}