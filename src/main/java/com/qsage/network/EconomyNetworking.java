package com.qsage.network;

import com.qsage.SmartEconomy;

import com.qsage.economy.market.ExchangeService;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.server.level.ServerPlayer;

public final class EconomyNetworking {

    private EconomyNetworking() {
    }

    public static void registerPayloads() {
        PayloadTypeRegistry.clientboundPlay().register(
                BalancePayload.TYPE,
                BalancePayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                ExchangeSnapshotPayload.TYPE,
                ExchangeSnapshotPayload.CODEC
        );
    }

    public static void syncBalance(
            ServerPlayer player
    ) {

        long available =
                SmartEconomy.economy()
                        .getAvailable(player.getUUID());

        long locked =
                SmartEconomy.economy()
                        .getLocked(player.getUUID());

        ServerPlayNetworking.send(
                player,
                new BalancePayload(
                        available,
                        locked
                )
        );
    }

    public static void syncExchange(ServerPlayer player) {

        ExchangeService exchangeService =
                SmartEconomy.getExchangeService();

        ExchangeSnapshotPayload payload =
                exchangeService.createSnapshot();

        ServerPlayNetworking.send(
                player,
                payload
        );
        System.out.println(
                "Sending exchange snapshot: "
                        + payload.quotes().size()
                        + " quotes to "
                        + player.getName().getString()
        );
    }
}