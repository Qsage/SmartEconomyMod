package com.qsage.economy.market.pricing;

public enum VolumeWindow {

    FIVE_MINUTES(5 * 60_000L),
    FIFTEEN_MINUTES(15 * 60_000L),
    ONE_HOUR(60 * 60_000L),
    SIX_HOURS(6 * 60 * 60_000L),
    TWENTY_FOUR_HOURS(24 * 60 * 60_000L);

    private final long durationMillis;

    VolumeWindow(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public long durationMillis() {
        return durationMillis;
    }
}