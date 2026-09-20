package com.qsage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.terraformersmc.modmenu.config.ModMenuConfigManager.save;

public final class SmartEconomyConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("smart-economy.json");

    private static SmartEconomyConfig config = SmartEconomyConfig.defaults();

    private SmartEconomyConfigManager() {
    }

    public static void load(){
        try{
            if(Files.notExists(CONFIG_PATH)){
                config = SmartEconomyConfig.defaults();
                save();
                return;
            }

            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)){
                SmartEconomyConfig loaded = GSON.fromJson(
                        reader,
                        SmartEconomyConfig.class
                );

                if(loaded == null){
                    config = SmartEconomyConfig.defaults();
                } else {
                    config = loaded.validate();
                }
            }
        } catch(Exception e){
            System.err.println("Failed to load config: "
                    + e.getMessage()
            );

            config = SmartEconomyConfig.defaults();
        }
    }

    public static void save(){
        try{
            Files.createDirectories(CONFIG_PATH.getParent());

            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)){
                GSON.toJson(config, writer);
            }
        } catch(IOException e){
            System.err.println("Failed to save config: "
            + e.getMessage()
            );
        }
    }

    public static SmartEconomyConfig get(){
        return config;
    }

    public static void set(SmartEconomyConfig newConfig){
        config = newConfig.validate();
    }

    public static void reset(){
        config = SmartEconomyConfig.defaults();
    }

    public static Path getConfigPath(){
        return CONFIG_PATH;
    }

}
