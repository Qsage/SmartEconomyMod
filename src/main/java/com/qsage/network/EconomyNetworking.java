package com.qsage.network;

import com.qsage.SmartEconomy;
import com.qsage.economy.bank.network.BankFavoriteTogglePayload;
import com.qsage.economy.bank.network.BankFavoritesSnapshotPayload;
import com.qsage.economy.market.network.ExchangeRefreshPayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import com.qsage.economy.market.network.ExchangeTradePayload;
import com.qsage.economy.notification.network.EconomyNotificationPayload;
import com.qsage.economy.notification.network.OfflineNotificationsAckPayload;
import com.qsage.economy.notification.network.OfflineNotificationsPayload;
import com.qsage.economy.npc.dialogue.NpcDialogueService;
import com.qsage.economy.npc.dialogue.network.NpcDialogueActionPayload;
import com.qsage.economy.npc.dialogue.network.NpcDialogueOpenPayload;
import com.qsage.economy.player.KnownPlayer;
import com.qsage.economy.player.network.*;
import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.network.TransactionHistoryRequestPayload;
import com.qsage.economy.transaction.network.TransactionHistorySnapshotPayload;
import com.qsage.economy.util.NumberFormatter;
import com.qsage.server.skin.ServerSkinStorage;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EconomyNetworking {
    private static final long SKIN_UPLOAD_COOLDOWN_MS = 5_000L;

    private EconomyNetworking() {
    }

    public static void registerPayloads() {

        PayloadTypeRegistry.clientboundPlay().register(
                BalancePayload.TYPE,
                BalancePayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                EconomyNotificationPayload.TYPE,
                EconomyNotificationPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                ExchangeSnapshotPayload.TYPE,
                ExchangeSnapshotPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                KnownPlayersSnapshotPayload.TYPE,
                KnownPlayersSnapshotPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                BankFavoritesSnapshotPayload.TYPE,
                BankFavoritesSnapshotPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                BankFavoriteTogglePayload.TYPE,
                BankFavoriteTogglePayload.CODEC
        );


        PayloadTypeRegistry.clientboundPlay().register(
                SkinSyncPayload.TYPE,
                SkinSyncPayload.CODEC
        );


        PayloadTypeRegistry.serverboundPlay().register(
                ExchangeTradePayload.TYPE,
                ExchangeTradePayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                ExchangeRefreshPayload.TYPE,
                ExchangeRefreshPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                PlayerTransferPayload.TYPE,
                PlayerTransferPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                KnownPlayersRequestPayload.TYPE,
                KnownPlayersRequestPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                OfflineNotificationsPayload.TYPE,
                OfflineNotificationsPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                OfflineNotificationsAckPayload.TYPE,
                OfflineNotificationsAckPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                TransactionHistoryRequestPayload.TYPE,
                TransactionHistoryRequestPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                TransactionHistorySnapshotPayload.TYPE,
                TransactionHistorySnapshotPayload.CODEC
        );

        PayloadTypeRegistry.clientboundPlay().register(
                NpcDialogueOpenPayload.TYPE,
                NpcDialogueOpenPayload.CODEC
        );

        PayloadTypeRegistry.serverboundPlay().register(
                NpcDialogueActionPayload.TYPE,
                NpcDialogueActionPayload.CODEC
        );

        ServerPlayNetworking.registerGlobalReceiver(
                NpcDialogueActionPayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() -> {
                        NpcDialogueService.handleAction(
                                context.player(),
                                payload.npcId(),
                                payload.optionId()
                        );
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                TransactionHistoryRequestPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        try {
                            int page = payload.page();

                            if (page < 0 || page > 100_000) {
                                return;
                            }

                            int pageSize = 20;
                            int offset = page * pageSize;

                            List<Transaction> history =
                                    SmartEconomy.economy().getTransactionHistory(
                                            player.getUUID(),
                                            pageSize + 1,
                                            offset
                                    );

                            boolean hasNextPage = history.size() > pageSize;

                            List<Transaction> visibleHistory =
                                    history.subList(
                                            0,
                                            Math.min(pageSize, history.size())
                                    );

                            ServerPlayNetworking.send(
                                    player,
                                    new TransactionHistorySnapshotPayload(
                                            page,
                                            hasNextPage,
                                            List.copyOf(visibleHistory)
                                    )
                            );

                        } catch (Exception e) {
                            SmartEconomy.LOGGER.error(
                                    "Failed to load transaction history for {}",
                                    player.getUUID(),
                                    e
                            );
                        }
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                OfflineNotificationsAckPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        SmartEconomy.acknowledgeOfflineNotifications(
                                player.getUUID(),
                                payload.lastNotificationId()
                        );
                    });
                }
        );

        ServerPlayNetworking.registerGlobalReceiver(
                BankFavoriteTogglePayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        try {
                            UUID target = payload.target();

                            // Нельзя добавлять самого себя.
                            if (player.getUUID().equals(target)) {
                                return;
                            }

                            // Проверяем, что игрок существует в базе.
                            KnownPlayer knownPlayer =
                                    SmartEconomy.getKnownPlayerService()
                                            .get(target);

                            if (knownPlayer == null) {
                                SmartEconomy.LOGGER.warn(
                                        "Unknown bank favorite target: {}",
                                        target
                                );
                                return;
                            }

                            // Изменяем состояние на сервере.
                            SmartEconomy.getBankFavoriteService()
                                    .toggle(player.getUUID(), target);

                        } catch (Exception e) {
                            SmartEconomy.LOGGER.error(
                                    "Failed to toggle bank favorite for {}",
                                    player.getUUID(),
                                    e
                            );
                        } finally {
                            // Отправляем подтверждённое состояние.
                            syncBankFavorites(player);
                        }
                    });
                }
        );


        ServerPlayNetworking.registerGlobalReceiver(
                KnownPlayersRequestPayload.TYPE,
                (payload, context) -> {
                    context.server().execute(() -> {
                        syncKnownPlayers(context.player());
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


        PayloadTypeRegistry.serverboundPlay().register(
                SkinUploadPayload.TYPE,
                SkinUploadPayload.CODEC
        );


        ServerPlayNetworking.registerGlobalReceiver(
                SkinUploadPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer player = context.player();

                    context.server().execute(() -> {
                        UUID uuid = player.getUUID();

                        long now = System.currentTimeMillis();
                        Long last = LAST_SKIN_UPLOAD.get(uuid);

                        if (last != null
                                && now - last < SKIN_UPLOAD_COOLDOWN_MS) {
                            return;
                        }

                        LAST_SKIN_UPLOAD.put(uuid, now);

                        try {
                            Path economyDirectory = context.server()
                                    .getWorldPath(LevelResource.ROOT)
                                    .resolve("economy");

                            boolean saved = ServerSkinStorage.save(
                                    economyDirectory,
                                    uuid,
                                    payload.png()
                            );


                            if (saved) {
                                SmartEconomy.LOGGER.info(
                                        "Saved skin for player {} ({} bytes)",
                                        uuid,
                                        payload.png().length
                                );

                                for (ServerPlayer recipient :
                                        context.server().getPlayerList().getPlayers()) {

                                    ServerPlayNetworking.send(
                                            recipient,
                                            new SkinSyncPayload(uuid, payload.png())
                                    );
                                }
                            }else {
                                SmartEconomy.LOGGER.warn(
                                        "Rejected skin upload from {}",
                                        uuid
                                );
                            }

                        } catch (Exception e) {
                            SmartEconomy.LOGGER.error(
                                    "Failed to save player skin: " + uuid,
                                    e
                            );
                        }
                    });
                }
        );


        ServerPlayNetworking.registerGlobalReceiver(
                PlayerTransferPayload.TYPE,
                (payload, context) -> {

                    ServerPlayer sender = context.player();

                    context.server().execute(() -> {
                        try {
                            // Проверяем получателя
                            KnownPlayer recipient =
                                    SmartEconomy.getKnownPlayerService()
                                            .get(payload.target());

                            if (recipient == null) {
                                throw new IllegalArgumentException(
                                        "Unknown player"
                                );
                            }

                            // Выполняем перевод
                            SmartEconomy.economy().transfer(
                                    sender.getUUID(),
                                    payload.target(),
                                    payload.amount(),
                                    "Player transfer"
                            );

                            syncBalance(sender);

                            // Сообщаем отправителю
                            sender.sendSystemMessage(
                                    Component.literal(
                                            "Successfully transferred "
                                                    + payload.amount()
                                                    + " to "
                                                    + recipient.name()
                                    )
                            );


                            ServerPlayer onlineRecipient =
                                    context.server()
                                            .getPlayerList()
                                            .getPlayer(payload.target());

                            if (onlineRecipient != null) {

                                String senderName = sender.getGameProfile().name();

                                String amountText = NumberFormatter.formatCompact(
                                        payload.amount()
                                );

                                String message =
                                        "+" + amountText + " $ от " + senderName;

                                ServerPlayNetworking.send(
                                        onlineRecipient,
                                        new EconomyNotificationPayload(
                                                "SUCCESS",
                                                "gui.smart_economy.notification.transfer_received",
                                                "gui.smart_economy.notification.message",
                                                message
                                        )
                                );

                                syncBalance(onlineRecipient);

                            } else {

                                // Получатель офлайн — сохраняем уведомление
                                try {
                                    SmartEconomy.getNotificationRepository()
                                            .addPendingNotification(
                                                    payload.target(),
                                                    sender.getUUID(),
                                                    payload.amount()
                                            );
                                } catch (Exception notificationError) {
                                    SmartEconomy.LOGGER.error(
                                            "Transfer completed, but offline notification could not be saved",
                                            notificationError
                                    );
                                }
                            }


                        } catch (Exception e) {
                            SmartEconomy.LOGGER.error(
                                    "Player transfer failed",
                                    e
                            );

                            sender.sendSystemMessage(
                                    Component.literal(
                                            "Transfer failed: " + e.getMessage()
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


    public static void syncKnownPlayers(ServerPlayer player) {
        KnownPlayersSnapshotPayload payload =
                SmartEconomy.getKnownPlayerService()
                        .createSnapshot(player.level().getServer());

        ServerPlayNetworking.send(player, payload);

        syncKnownPlayerSkins(player);

        syncBankFavorites(player);
    }


    public static void syncKnownPlayersOnJoin(
            MinecraftServer server,
            ServerPlayer joiningPlayer
    ) {
        KnownPlayersSnapshotPayload payload =
                SmartEconomy.getKnownPlayerService()
                        .createJoinSnapshot(
                                server,
                                joiningPlayer.getUUID()
                        );

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }


    public static void syncBankFavorites(ServerPlayer player) {

        var favorites =
                SmartEconomy.getBankFavoriteService()
                        .getFavorites(player.getUUID());

        ServerPlayNetworking.send(
                player,
                new BankFavoritesSnapshotPayload(favorites)
        );
    }


    public static void syncKnownPlayersToAll(
            MinecraftServer server
    ) {
        KnownPlayersSnapshotPayload payload =
                SmartEconomy.getKnownPlayerService()
                        .createSnapshot(server);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void syncKnownPlayersToAll(
            MinecraftServer server,
            UUID disconnectingUuid
    ) {
        KnownPlayersSnapshotPayload payload =
                SmartEconomy.getKnownPlayerService()
                        .createSnapshot(server, disconnectingUuid);

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.getUUID().equals(disconnectingUuid)) {
                continue;
            }

            ServerPlayNetworking.send(player, payload);
        }
    }


    public static void syncSkin(
            ServerPlayer recipient,
            UUID skinOwner
    ) {
        try {
            Path economyDirectory = recipient.level()
                    .getServer()
                    .getWorldPath(LevelResource.ROOT)
                    .resolve("economy");

            byte[] png = ServerSkinStorage.load(
                    economyDirectory,
                    skinOwner
            );

            if (png == null) {
                return;
            }

            ServerPlayNetworking.send(
                    recipient,
                    new SkinSyncPayload(skinOwner, png)
            );

        } catch (IOException e) {
            SmartEconomy.LOGGER.error(
                    "Failed to sync skin for " + skinOwner,
                    e
            );
        }
    }


    public static void syncKnownPlayerSkins(
            ServerPlayer recipient
    ) {
        KnownPlayersSnapshotPayload snapshot =
                SmartEconomy.getKnownPlayerService()
                        .createSnapshot(recipient.level().getServer());

        for (KnownPlayersSnapshotPayload.KnownPlayerData player
                : snapshot.players()) {

            syncSkin(recipient, player.uuid());
        }
    }


    private static final Map<UUID, Long> LAST_SKIN_UPLOAD =
            new HashMap<>();


}