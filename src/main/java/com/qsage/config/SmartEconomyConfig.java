package com.qsage.config;

public record SmartEconomyConfig(
        MarketConfig market,
        PricingConfig pricing,
        InflationConfig inflation,
        TradingConfig trading
) {
    public static SmartEconomyConfig defaults() {
        return new SmartEconomyConfig(
                MarketConfig.defaults(),
                PricingConfig.defaults(),
                InflationConfig.defaults(),
                TradingConfig.defaults()
        );
    }

    public SmartEconomyConfig validate() {
        return new SmartEconomyConfig(
                market.validate(),
                pricing.validate(),
                inflation.validate(),
                trading.validate()
        );
    }
}