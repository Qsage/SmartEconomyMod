package com.qsage;

import com.qsage.economy.EconomyService;
import com.qsage.economy.command.EconomyCommands;
import com.qsage.economy.storage.EconomyDatabase;
import com.qsage.economy.storage.SqliteEconomyRepository;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartEconomy implements ModInitializer {

	public static final String MOD_ID = "smart-economy";

	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	private static EconomyService economy;

	private static EconomyDatabase database;

	@Override
	public void onInitialize() {

		EconomyCommands.register();

		ServerLifecycleEvents.SERVER_STARTED.register(
				server -> {

					database =
							new EconomyDatabase(server);

					SqliteEconomyRepository repository =
							new SqliteEconomyRepository(database);

					economy =
							new EconomyService(repository);

					LOGGER.info(
							"Smart Economy database initialized at {}",
							database.databasePath()
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