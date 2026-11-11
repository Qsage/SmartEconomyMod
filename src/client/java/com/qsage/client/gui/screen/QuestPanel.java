package com.qsage.client.gui.screen;

import com.qsage.client.gui.component.GuiComponent;
import com.qsage.client.gui.component.quest.QuestWidgetData;

import java.util.ArrayList;
import java.util.List;

public final class QuestPanel {

    private static final int ROW_X = 18;
    private static final int ROW_Y = 21;
    private static final int ROW_HEIGHT = 58;
    private static final int VISIBLE_ROWS = 2;

    private final EconomyScreen screen;
    private final List<GuiComponent> questWidgets =
            new ArrayList<>();

    private List<QuestWidgetData> quests = List.of();

    private QuestCategory selectedCategory =
            QuestCategory.AVAILABLE;

    private int scrollOffset = 0;
    private boolean visible = false;
    private boolean npcMode = false;

    public QuestPanel(EconomyScreen screen) {
        this.screen = screen;
    }

    public void init() {
        rebuildQuestWidgets();
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (visible) {
            rebuildQuestWidgets();
        } else {
            screen.clearGuiComponents(questWidgets);
        }
    }

    public void setNpcMode(boolean npcMode) {
        this.npcMode = npcMode;

        if (visible) {
            rebuildQuestWidgets();
        }
    }

    public void setQuests(List<QuestWidgetData> quests) {
        this.quests = List.copyOf(quests);
        scrollOffset = 0;

        if (visible) {
            rebuildQuestWidgets();
        }
    }

    private List<QuestWidgetData> getFilteredQuests() {
        return quests.stream()
                .filter(quest -> switch (selectedCategory) {
                    case AVAILABLE ->
                            quest.status() ==
                                    QuestWidgetData.Status.AVAILABLE;

                    case ACTIVE ->
                            quest.status() ==
                                    QuestWidgetData.Status.ACTIVE
                                    || quest.status() ==
                                    QuestWidgetData.Status.READY_TO_CLAIM;
                })
                .toList();
    }

    private void rebuildQuestWidgets() {
        screen.clearGuiComponents(questWidgets);

        if (!visible) return;

        List<QuestWidgetData> filtered = getFilteredQuests();

        int maxScroll = Math.max(
                0,
                filtered.size() - VISIBLE_ROWS
        );

        scrollOffset = Math.max(
                0,
                Math.min(scrollOffset, maxScroll)
        );

        int end = Math.min(
                scrollOffset + VISIBLE_ROWS,
                filtered.size()
        );

        for (int index = scrollOffset; index < end; index++) {

            QuestWidgetData quest = filtered.get(index);

            // Здесь создадим QuestWidget
            // после подключения его отрисовки.

        }
    }

    private enum QuestCategory {
        AVAILABLE,
        ACTIVE
    }
}
