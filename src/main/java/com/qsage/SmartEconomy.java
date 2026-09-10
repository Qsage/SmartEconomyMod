package com.qsage;

import com.qsage.economy.EconomyService;
import com.qsage.economy.command.EconomyCommands;
import com.qsage.economy.market.ExchangeService;
import com.qsage.economy.market.model.ExchangeAsset;
import com.qsage.economy.storage.EconomyDatabase;
import com.qsage.economy.storage.SqliteEconomyRepository;

import com.qsage.network.EconomyNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartEconomy implements ModInitializer {

	public static final String MOD_ID = "smart-economy";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	private static EconomyService economy;

	private static ExchangeService exchangeService;

	private static EconomyDatabase database;

	public static ExchangeService getExchangeService() {
		if (exchangeService == null) {
			throw new IllegalStateException(
					"ExchangeService is not initialized"
			);
		}

		return exchangeService;
	}

	@Override
	public void onInitialize() {

		EconomyNetworking.registerPayloads();

		EconomyCommands.register();

		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> {

					ExchangeService exchangeService = new ExchangeService();

					exchangeService.loadAssets(
							server.getResourceManager()
					);
					SmartEconomy.exchangeService = exchangeService;

					System.out.println(
							"Loaded exchange assets: "
									+ exchangeService.getAssets().size()
					);

					for (ExchangeAsset asset : exchangeService.getAssets().values()) {
						System.out.println(
								"Exchange asset: "
										+ asset.itemId()
										+ " | base="
										+ asset.basePrice()
										+ " | value="
										+ ((double) asset.valuePpm()
										/ ExchangeAsset.VALUE_SCALE)
										+ " | fundamental="
										+ asset.fundamentalPrice()
						);
					}

					database =
							new EconomyDatabase(server);

					SqliteEconomyRepository repository =
							new SqliteEconomyRepository(
									database
							);

					economy =
							new EconomyService(
									repository,
									playerId -> {

										ServerPlayer player =
												server.getPlayerList()
														.getPlayer(playerId);

										if (player != null) {
											EconomyNetworking
													.syncBalance(player);
										}
									}
							);



					LOGGER.info(
							"Smart Economy server initialized"
					);
				}
		);

		ServerPlayConnectionEvents.JOIN.register(
				(listener, sender, server) -> {

					EconomyNetworking.syncBalance(
							listener.getPlayer()
					);

					EconomyNetworking.syncExchange(
							listener.getPlayer()
					);
				}
		);

		ServerLifecycleEvents.SERVER_STOPPING.register(
				server -> {

					if (database != null) {
						database.close();
						database = null;
					}

					economy = null;

					LOGGER.info(
							"Smart Economy database closed"
					);
				}
		);

		LOGGER.info(
				"Smart Economy initialized"
		);
	}


	public static EconomyService economy() {

		if (economy == null) {
			throw new IllegalStateException(
					"EconomyService is not initialized"
			);
		}

		return economy;
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(
				MOD_ID,
				path
		);
	}
}