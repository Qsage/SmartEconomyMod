package com.qsage.client.economy;

import java.util.Locale;

public final class NumberFormatter {

    private static final long[] THRESHOLDS = {
            1_000L,
            1_000_000L,
            1_000_000_000L,
            1_000_000_000_000L,
            1_000_000_000_000_000L,
            1_000_000_000_000_000_000L
    };

    private static final String[] SUFFIXES = {
            "к",
            "кк",
            "ккк",
            "кккк",
            "ккккк",
            "кккккк"
    };

    private NumberFormatter() {
    }

    public static String formatCompact(long amount) {
        if (amount < 1_000L) {
            return Long.toString(amount);
        }

        for (int i = THRESHOLDS.length - 1; i >= 0; i--) {
            if (amount >= THRESHOLDS[i]) {
                double value = (double) amount / THRESHOLDS[i];

                return String.format(
                        Locale.ROOT,
                        "%.2f%s",
                        value,
                        SUFFIXES[i]
                );
            }
        }

        return Long.toString(amount);
    }

    public static String formatFull(long amount) {
        return String.format(
                Locale.ROOT,
                "%,d",
                amount
        ).replace(',', '.');
    }
}