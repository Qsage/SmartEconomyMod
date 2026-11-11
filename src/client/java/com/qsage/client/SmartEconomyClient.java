package com.qsage.client;

import com.qsage.SmartEconomy;
import com.qsage.client.economy.ClientEconomy;
import com.qsage.client.economy.ClientTransactionHistory;
import com.qsage.client.economy.MoneyFormatter;
import com.qsage.client.gui.TestGuiKeybind;
import com.qsage.client.gui.component.toasts.EconomyNotifications;
import com.qsage.client.gui.component.toasts.EconomyToast;
import com.qsage.client.gui.screen.EconomyScreen;
import com.qsage.client.market.ClientExchange;
import com.qsage.client.player.ClientBankFavorites;
import com.qsage.client.player.ClientKnownPlayers;
import com.qsage.client.player.ClientPlayerSkins;
import com.qsage.client.skin.ClientSkinRegistry;
import com.qsage.client.skin.ClientSkinUploader;
import com.qsage.economy.bank.network.BankFavoritesSnapshotPayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import com.qsage.economy.notification.network.EconomyNotificationPayload;
import com.qsage.economy.notification.network.OfflineNotificationsAckPayload;
import com.qsage.economy.notification.network.OfflineNotificationsPayload;
import com.qsage.economy.player.network.KnownPlayersSnapshotPayload;
import com.qsage.economy.player.network.SkinSyncPayload;
import com.qsage.economy.transaction.network.TransactionHistorySnapshotPayload;
import com.qsage.network.BalancePayload;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;

public final class SmartEconomyClient
		implements ClientModInitializer {



	@Override
	public void onInitializeClient() {

		TestGuiKeybind.register();
		ClientSkinUploader.register();

		ClientPlayNetworking.registerGlobalReceiver(
				BalancePayload.TYPE,
				(payload, context) -> {

					context.client().execute(() -> {

						ClientEconomy.setBalance(
								payload.available(),
								payload.locked()
						);

					});
				}
		);

		ClientPlayNetworking.registerGlobalReceiver(
				ExchangeSnapshotPayload.TYPE,
				(payload, context) ->
						context.client().execute(() -> {

							ClientExchange.update(payload);

							if (context.client().gui.screen()
									instanceof EconomyScreen screen) {

								screen.refreshExchange();
							}
						})
		);

		ClientPlayNetworking.registerGlobalReceiver(
				EconomyNotificationPayload.TYPE,
				(payload, context) -> {

					context.client().execute(() -> {

						EconomyToast.Type type;

						try {
							type = EconomyToast.Type.valueOf(
									payload.notificationType()
							);
						} catch (IllegalArgumentException e) {
							type = EconomyToast.Type.INFO;
						}

						EconomyNotifications.show(
								type,
								Component.translatable(payload.titleKey()),
								Component.translatable(
										payload.messageKey(),
										payload.argument()
								)
						);
					});
				}
		);

		ClientPlayNetworking.registerGlobalReceiver(
				TransactionHistorySnapshotPayload.TYPE,
				(payload, context) -> {

					context.client().execute(() -> {

						ClientTransactionHistory.update(payload);

						if (context.client().gui.screen()
								instanceof EconomyScreen screen) {

							screen.refreshTransactionHistory();
						}
					});
				}
		);


		ClientPlayNetworking.registerGlobalReceiver(
				OfflineNotificationsPayload.TYPE,
				(payload, context) -> context.client().execute(() -> {

					String amountText = MoneyFormatter.format(
							payload.totalAmount()
					);

					EconomyNotifications.show(
							EconomyToast.Type.SUCCESS,
							Component.translatable(
									"gui.smart_economy.notification.offline_received"
							),
							Component.translatable(
									"gui.smart_economy.notification.offline_summary",
									amountText,
									payload.transferCount()
							)
					);

					ClientPlayNetworking.send(
							new OfflineNotificationsAckPayload(
									payload.lastNotificationId()
							)
					);
				})
		);


		ClientPlayNetworking.registerGlobalReceiver(
				KnownPlayersSnapshotPayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {

						ClientKnownPlayers.update(payload);
						if (context.client().gui.screen()
								instanceof EconomyScreen screen) {
							screen.refreshBank();
						}
					});
				}
		);


		ClientPlayNetworking.registerGlobalReceiver(
				BankFavoritesSnapshotPayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {

						ClientBankFavorites.update(payload);

						if (context.client().gui.screen()
								instanceof EconomyScreen screen) {

							screen.refreshBank();
						}
					});
				}
		);



		ClientPlayNetworking.registerGlobalReceiver(
				SkinSyncPayload.TYPE,
				(payload, context) -> {
					context.client().execute(() -> {
						ClientSkinRegistry.receive(
								payload.uuid(),
								payload.png()
						);
					});
				}
		);




		ClientPlayConnectionEvents.DISCONNECT.register(
				(handler, client) -> {
					ClientEconomy.clear();
					ClientExchange.clear();
					ClientKnownPlayers.clear();
					ClientPlayerSkins.clear();
					ClientBankFavorites.clear();
					ClientTransactionHistory.clear();
				}
		);
	}
}