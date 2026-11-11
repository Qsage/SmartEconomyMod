package com.qsage;


import com.qsage.config.SmartEconomyConfigManager;
import com.qsage.economy.EconomyService;
import com.qsage.economy.bank.BankFavoriteService;
import com.qsage.economy.command.EconomyCommands;
import com.qsage.economy.market.ExchangeRepository;
import com.qsage.economy.market.ExchangeService;
import com.qsage.economy.market.storage.SqliteExchangeRepository;
import com.qsage.economy.player.KnownPlayerService;
import com.qsage.economy.player.skin.PlayerSkinRepository;
import com.qsage.economy.player.skin.ServerPlayerSkinService;
import com.qsage.economy.storage.*;

import com.qsage.network.EconomyNetworking;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;

import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class SmartEconomy implements ModInitializer {

	public static final String MOD_ID = "smart-economy";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	private static EconomyService economy;
	private static ExchangeService exchangeService;

	private static ExchangeRepository exchangeRepository;
	private static EconomyDatabase database;

	private static PlayerSkinRepository playerSkinRepository;
	private static ServerPlayerSkinService playerSkinService;

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

	private static BankFavoriteService bankFavoriteService;

	public static BankFavoriteService getBankFavoriteService() {
		if (bankFavoriteService == null) {
			throw new IllegalStateException(
					"BankFavoriteService is not initialized"
			);
		}

		return bankFavoriteService;
	}


	@Override
	public void onInitialize() {
		SmartEconomyConfigManager.load();

		EconomyNetworking.registerPayloads();

		EconomyCommands.register();


		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			database = new EconomyDatabase(server);

			SqliteEconomyRepository economyRepository =
					new SqliteEconomyRepository(database);


			SqliteKnownPlayerRepository knownPlayerRepository =
					new SqliteKnownPlayerRepository(database.connection());

			playerSkinRepository = new SqlitePlayerSkinRepository(
					database.connection()
			);

			playerSkinService = new ServerPlayerSkinService(
					playerSkinRepository
			);

			knownPlayerService =
					new KnownPlayerService(knownPlayerRepository);

			SqliteBankFavoriteRepository favoriteRepository =
					new SqliteBankFavoriteRepository(database);

			bankFavoriteService = new BankFavoriteService(
					favoriteRepository
			);

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

			exchangeRepository =
					new SqliteExchangeRepository(database);

			exchangeService =
					new ExchangeService(
							exchangeRepository,
							economyService
					);

			exchangeService.loadAssets(
					server.getResourceManager()
			);

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

					ServerPlayer player = handler.getPlayer();

					knownPlayerService.playerJoined(player);

					EconomyNetworking.syncKnownPlayersOnJoin(
							server,
							player
					);

					EconomyNetworking.syncBalance(player);
					EconomyNetworking.syncExchange(player);
				}
		);


		ServerPlayConnectionEvents.DISCONNECT.register(
				(handler, server) -> {

					UUID disconnectingUuid =
							handler.getPlayer().getUUID();

					server.execute(() ->
							EconomyNetworking.syncKnownPlayersToAll(
									server,
									disconnectingUuid
							)
					);
				}
		);

		ServerLifecycleEvents.SERVER_STOPPING.register(
				server -> {

					if (database != null) {
						database.close();
						database = null;
					}

					knownPlayerService = null;
					playerSkinService = null;
					playerSkinRepository = null;

					economy = null;
					exchangeService = null;
					exchangeRepository = null;

					bankFavoriteService = null;

					LOGGER.warn(
							"Smart Economy database closed"
					);
				}
		);

		LOGGER.warn(
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

	public static PlayerSkinRepository getPlayerSkinRepository() {
		return playerSkinRepository;
	}

	public static ServerPlayerSkinService getPlayerSkinService() {
		return playerSkinService;
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