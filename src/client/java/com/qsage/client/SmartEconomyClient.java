package com.qsage.client;

import com.qsage.SmartEconomy;
import com.qsage.client.economy.ClientEconomy;
import com.qsage.client.gui.TestGuiKeybind;
import com.qsage.client.market.ClientExchange;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import com.qsage.network.BalancePayload;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public final class SmartEconomyClient
		implements ClientModInitializer {

	@Override
	public void onInitializeClient() {

		TestGuiKeybind.register();

		ClientPlayNetworking.registerGlobalReceiver(
				BalancePayload.TYPE,
				(payload, context) -> {

					context.client().execute(() -> {

						ClientEconomy.setBalance(
								payload.available(),
								payload.locked()
						);

						SmartEconomy.LOGGER.debug(
								"Received balance: available={}, locked={}",
								payload.available(),
								payload.locked()
						);
					});
				}
		);
		ClientPlayNetworking.registerGlobalReceiver(
				ExchangeSnapshotPayload.TYPE,
				(payload, context) -> {

					System.out.println(
							"Received exchange snapshot: "
									+ payload.quotes().size()
					);

					context.client().execute(() -> {
						ClientExchange.update(payload);
					});
				}
		);

		ClientPlayConnectionEvents.DISCONNECT.register(
				(handler, client) -> {
					ClientEconomy.clear();
					ClientExchange.clear();
				}
		);
	}
}