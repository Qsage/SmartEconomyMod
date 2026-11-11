package com.qsage.economy.wallet.network;

import java.util.UUID;

public record BankPlayerInfo(
        UUID uuid,
        String name,
        boolean online,
        boolean favorite
) {
}