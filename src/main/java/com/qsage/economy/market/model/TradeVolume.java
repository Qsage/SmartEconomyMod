package com.qsage.economy.market.model;

public record TradeVolume(
        long buyVolume,
        long sellVolume,
        long buyTrades,
        long sellTrades
) {

    public TradeVolume {
        if (buyVolume < 0) {
            throw new IllegalArgumentException("Buy volume cannot be negative");
        }

        if (sellVolume < 0) {
            throw new IllegalArgumentException("Sell volume cannot be negative");
        }

        if (buyTrades < 0) {
            throw new IllegalArgumentException("Buy trades cannot be negative");
        }

        if (sellTrades < 0) {
            throw new IllegalArgumentException("Sell trades cannot be negative");
        }
    }

    public long totalVolume() {
        return Math.addExact(buyVolume, sellVolume);
    }

    public long netVolume() {
        return buyVolume - sellVolume;
    }

    public static TradeVolume empty() {
        return new TradeVolume(0, 0, 0, 0);
    }
}