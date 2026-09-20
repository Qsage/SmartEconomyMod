package com.qsage.network;

import com.qsage.SmartEconomy;
import com.qsage.economy.market.network.ExchangeRefreshPayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import com.qsage.economy.market.network.ExchangeTradePayload;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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

        PayloadTypeRegistry.serverboundPlay().register(
                ExchangeTradePayload.TYPE,
                ExchangeTradePayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                ExchangeRefreshPayload.TYPE,
                ExchangeRefreshPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ExchangeTradePayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        try {
                            SmartEconomy.getExchangeService().trade(
                                    context.server(),
                                    player,
                                    payload.itemId(),
                                    payload.side(),
                                    payload.quantity()
                            );

                            syncExchangeToAll(context.server());
                            syncBalance(player);

                        } catch (Exception e) {
                            e.printStackTrace();

                            player.sendSystemMessage(
                                    Component.literal("Trade failed: " + e.getMessage())
                            );
                        }
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ExchangeRefreshPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        try {
                            SmartEconomy.getExchangeService().refreshQuotes();

                            syncExchangeToAll(context.server());

                        } catch (Exception e) {
                            e.printStackTrace();

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Exchange refresh failed: " + e.getMessage()
                                    )
                            );
                        }
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                ExchangeTradePayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        try {

                            SmartEconomy.getExchangeService().trade(
                                    context.server(),
                                    player,
                                    payload.itemId(),
                                    payload.side(),
                                    payload.quantity()
                            );

                            // Обновляем exchange у всех игроков
                            syncExchangeToAll(context.server());

                            // Баланс меняется только у этого игрока
                            syncBalance(player);

                        } catch (Exception e) {
                            e.printStackTrace();

                            player.sendSystemMessage(
                                    Component.literal(
                                            "Trade failed: " + e.getMessage()
                                    )
                            );
                        }
                    });
                }
        );
    }



    public static void syncExchangeToAll(MinecraftServer server) {

        ExchangeSnapshotPayload payload =
                SmartEconomy.getExchangeService().createSnapshot();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }


    /*
     * ============================================================
     * Exchange sync
     * ============================================================
     */

    public static void syncExchange(
            ServerPlayer player
    ) {
        ServerPlayNetworking.send(
                player,
                SmartEconomy
                        .getExchangeService()
                        .createSnapshot()
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
}