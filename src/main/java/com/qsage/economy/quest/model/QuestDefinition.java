package com.qsage.economy.quest.model;

import net.minecraft.resources.Identifier;

import java.util.List;

public record QuestDefinition(
        Identifier id,
        String titleKey,
        String descriptionKey,
        List<QuestObjective> objectives,
        long rewardMoney,
        boolean repeatable
) {
    public QuestDefinition {
        if (id == null || titleKey == null || descriptionKey == null) {
            throw new IllegalArgumentException(
                    "Invalid quest definition"
            );
        }

        objectives = List.copyOf(objectives);

        if (objectives.isEmpty()) {
            throw new IllegalArgumentException(
                    "Quest must have at least one objective"
            );
        }

        if (rewardMoney < 0) {
            throw new IllegalArgumentException(
                    "Quest reward cannot be negative"
            );
        }
    }
}