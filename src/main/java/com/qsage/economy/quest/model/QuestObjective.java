package com.qsage.economy.quest.model;

import net.minecraft.resources.Identifier;

public record QuestObjective(
        Type type,
        Identifier itemId,
        int amount
) {
    public enum Type {
        DELIVER_ITEM
    }

    public QuestObjective {
        if (type == null || itemId == null) {
            throw new IllegalArgumentException(
                    "Quest objective type and item cannot be null"
            );
        }

        if (amount <= 0) {
            throw new IllegalArgumentException(
                    "Quest objective amount must be positive"
            );
        }
    }
}