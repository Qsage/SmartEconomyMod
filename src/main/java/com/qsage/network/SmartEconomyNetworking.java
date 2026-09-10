package com.qsage.network;

import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class SmartEconomyNetworking {

    private SmartEconomyNetworking() {
    }

    public static void register() {

        PayloadTypeRegistry.clientboundPlay()
                .register(
                        BalancePayload.TYPE,
                        BalancePayload.CODEC
                );


    }
}