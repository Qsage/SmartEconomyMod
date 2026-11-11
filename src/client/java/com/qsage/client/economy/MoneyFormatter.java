package com.qsage.client.economy;
import com.qsage.economy.util.NumberFormatter;

public final class MoneyFormatter {

    private MoneyFormatter() {
    }

    public static String format(long amount) {
        return NumberFormatter.formatCompact(amount) + " $";
    }
    public static String formatFull(long amount) {
        return NumberFormatter.formatFull(amount) + " $";
    }
}