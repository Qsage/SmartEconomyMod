package com.qsage.economy.market;

import net.minecraft.resources.Identifier;

public record ExchangeConfig(
        Identifier itemId,
        long basePrice,
        long valuePpm
) {
}