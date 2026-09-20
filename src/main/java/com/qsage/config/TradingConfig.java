package com.qsage.config;

public record TradingConfig(
        long maxTransactionQuantity,
        long maxTransactionValue,
        long cooldownTicks
) {

    public static TradingConfig defaults() {
        return new TradingConfig(
                64,
                1_000_000,
                10
        );
    }

    public TradingConfig validate() {
        return new TradingConfig(
                Math.max(1, maxTransactionQuantity),
                Math.max(1, maxTransactionValue),
                Math.max(0, cooldownTicks)
        );
    }
}