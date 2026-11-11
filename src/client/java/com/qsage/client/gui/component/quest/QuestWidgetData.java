package com.qsage.client.gui.component.quest;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record QuestWidgetData(
        String questId,
        Component title,
        Component description,
        List<Requirement> requirements,
        long reward,
        Status status
) {
    public QuestWidgetData {
        requirements = List.copyOf(requirements);
    }

    public record Requirement(
            ItemStack icon,
            int current,
            int required,
            Component tooltipDescription
    ) {
        public Requirement {
            icon = icon.copy();
        }

        public float progress() {
            if (required <= 0) return 0.0f;

            return Math.min(
                    1.0f,
                    (float) current / required
            );
        }

        public boolean complete() {
            return current >= required;
        }
    }

    public enum Status {
        AVAILABLE,
        ACTIVE,
        READY_TO_CLAIM,
        COMPLETED
    }
}