package com.qsage;


import com.qsage.config.SmartEconomyConfigManager;
import com.qsage.economy.EconomyService;
import com.qsage.economy.command.EconomyCommands;
import com.qsage.economy.market.ExchangeRepository;
import com.qsage.economy.market.ExchangeService;
import com.qsage.economy.market.storage.SqliteExchangeRepository;
import com.qsage.economy.player.KnownPlayerService;
import com.qsage.economy.storage.EconomyDatabase;
import com.qsage.economy.storage.SqliteEconomyRepository;

import com.qsage.economy.storage.SqliteKnownPlayerRepository;
import com.qsage.network.EconomyNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
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

	private static ExchangeRepository exchangeRepository;
	private static EconomyDatabase database;

	private static KnownPlayerService knownPlayerService;

	public static KnownPlayerService getKnownPlayerService() {
		return knownPlayerService;
	}

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
		SmartEconomyConfigManager.load();

		EconomyNetworking.registerPayloads();

		EconomyCommands.register();


		ServerLifecycleEvents.SERVER_STARTED.register(server -> {

			/*
			 * ============================================================
			 * Economy database
			 * ============================================================
			 */

			database = new EconomyDatabase(server);


			/*
			 * ============================================================
			 * Economy repository
			 * ============================================================
			 */

			SqliteEconomyRepository economyRepository =
					new SqliteEconomyRepository(database);


			SqliteKnownPlayerRepository knownPlayerRepository =
					new SqliteKnownPlayerRepository(database.connection());

			knownPlayerService =
					new KnownPlayerService(knownPlayerRepository);


			/*
			 * ============================================================
			 * Economy service
			 * ============================================================
			 */

			EconomyService economyService =
					new EconomyService(
							economyRepository,
							playerId -> {

								ServerPlayer player =
										server.getPlayerList()
												.getPlayer(playerId);

								if (player != null) {
									EconomyNetworking.syncBalance(
											player
									);
								}
							}
					);


			/*
			 * ============================================================
			 * Exchange repository
			 * ============================================================
			 */

			exchangeRepository =
					new SqliteExchangeRepository(database);


			/*
			 * ============================================================
			 * Exchange service
			 * ============================================================
			 */

			exchangeService =
					new ExchangeService(
							exchangeRepository,
							economyService
					);


			/*
			 * ============================================================
			 * Load exchange assets
			 * ============================================================
			 */

			exchangeService.loadAssets(
					server.getResourceManager()
			);


			/*
			 * ============================================================
			 * Save economy service
			 * ============================================================
			 */

			economy = economyService;


			LOGGER.info(
					"Smart Economy started"
			);

			LOGGER.info(
					"Loaded exchange assets: {}",
					exchangeService.getAssets().size()
			);
		});
		ServerPlayConnectionEvents.JOIN.register(
				(handler, sender, server) -> {

					ServerPlayer player =
							handler.getPlayer();

					knownPlayerService.playerJoined(player);

					EconomyNetworking.syncBalance(player);
					EconomyNetworking.syncExchange(player);
				}
		);

		ServerLifecycleEvents.SERVER_STOPPING.register(
				server -> {

					if (database != null) {
						database.close();
						database = null;
					}

					knownPlayerService = null;
					economy = null;
					exchangeService = null;
					exchangeRepository = null;

					LOGGER.info(
							"Smart Economy database closed"
					);
				}
		);

		LOGGER.info(
				"Smart Economy initialized"
		);
	}

	public static void syncExchange(
			ServerPlayer player
	) {
		ServerPlayNetworking.send(
				player,
				SmartEconomy.getExchangeService()
						.createSnapshot()
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