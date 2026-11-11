package com.qsage.economy.quest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.qsage.SmartEconomy;
import com.qsage.economy.quest.model.QuestDefinition;
import com.qsage.economy.quest.model.QuestObjective;

import net.minecraft.resources.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public final class QuestLoader {

    private static final String RESOURCE_PATH =
            "/data/smart-economy/quest/quests.json";

    private static Map<Identifier, QuestDefinition> quests = Map.of();

    private QuestLoader() {
    }

    public static void load() {
        try (InputStream stream =
                     QuestLoader.class.getResourceAsStream(RESOURCE_PATH)) {

            if (stream == null) {
                throw new IllegalStateException(
                        "Quest resource not found: " + RESOURCE_PATH
                );
            }

            JsonArray root = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            ).getAsJsonArray();

            Map<Identifier, QuestDefinition> loaded =
                    new LinkedHashMap<>();

            for (var element : root) {
                QuestDefinition quest = parseQuest(
                        element.getAsJsonObject()
                );

                if (loaded.putIfAbsent(quest.id(), quest) != null) {
                    throw new IllegalStateException(
                            "Duplicate quest ID: " + quest.id()
                    );
                }
            }

            quests = Map.copyOf(loaded);

            SmartEconomy.LOGGER.info(
                    "Loaded {} SmartEconomy quests",
                    quests.size()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load SmartEconomy quests", e
            );
        }
    }

    private static QuestDefinition parseQuest(JsonObject json) {

        Identifier id = Identifier.parse(
                json.get("id").getAsString()
        );

        List<QuestObjective> objectives = new ArrayList<>();

        for (var element : json.getAsJsonArray("objectives")) {

            JsonObject objective = element.getAsJsonObject();

            objectives.add(new QuestObjective(
                    QuestObjective.Type.valueOf(
                            objective.get("type").getAsString()
                    ),
                    Identifier.parse(
                            objective.get("item").getAsString()
                    ),
                    objective.get("amount").getAsInt()
            ));
        }

        JsonObject rewards = json.getAsJsonObject("rewards");

        return new QuestDefinition(
                id,
                json.get("title").getAsString(),
                json.get("description").getAsString(),
                objectives,
                rewards.get("money").getAsLong(),
                json.has("repeatable")
                        && json.get("repeatable").getAsBoolean()
        );
    }

    public static QuestDefinition get(Identifier id) {
        return quests.get(id);
    }

    public static Map<Identifier, QuestDefinition> getAll() {
        return quests;
    }
}