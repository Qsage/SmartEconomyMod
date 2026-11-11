package com.qsage.client.npc.dialogue;

import com.qsage.client.gui.screen.EconomyScreen;
import com.qsage.economy.npc.dialogue.network.NpcDialogueActionPayload;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NpcDialogueScreen extends Screen {

    private static final int GUI_WIDTH = 260;
    private static final int GUI_HEIGHT = 160;

    private final int npcId;
    private final String npcName;

    private String message;
    private boolean showOptions;

    public NpcDialogueScreen(
            int npcId,
            String npcName,
            String message,
            boolean showOptions
    ) {
        super(Component.literal(npcName));

        this.npcId = npcId;
        this.npcName = npcName;
        this.message = message;
        this.showOptions = showOptions;
    }

    private int guiLeft() {
        return (width - GUI_WIDTH) / 2;
    }

    private int guiTop() {
        return (height - GUI_HEIGHT) / 2;
    }

    @Override
    protected void init() {
        super.init();

        int x = guiLeft();
        int y = guiTop();

        if (showOptions) {

            addRenderableWidget(
                    Button.builder(
                            Component.literal("Покажи свои товары"),
                            button -> selectOption(0)
                    ).bounds(x + 20, y + 65, 220, 20).build()
            );

            addRenderableWidget(
                    Button.builder(
                            Component.literal("Есть работа для меня?"),
                            button -> selectOption(1)
                    ).bounds(x + 20, y + 90, 220, 20).build()
            );

            addRenderableWidget(
                    Button.builder(
                            Component.literal("До встречи"),
                            button -> selectOption(2)
                    ).bounds(x + 20, y + 115, 220, 20).build()
            );

        } else {

            addRenderableWidget(
                    Button.builder(
                            Component.literal("Вернуться назад"),
                            button -> returnToDialogue()
                    ).bounds(x + 60, y + 115, 140, 20).build()
            );
        }
    }

    private void returnToDialogue() {

        this.message =
                "Приветствую, путешественник! Чем могу помочь?";

        this.showOptions = true;

        rebuildWidgets();
    }

    private void selectOption(int optionId) {

        // Открываем биржу
        if (optionId == 0) {
            Minecraft.getInstance().gui.setScreen(
                    new EconomyScreen()
            );
            return;
        }

        // Закрываем диалог
        if (optionId == 2) {
            onClose();
            return;
        }

        // Остальные варианты обрабатывает сервер
        if (!ClientPlayNetworking.canSend(
                NpcDialogueActionPayload.TYPE
        )) {
            return;
        }

        ClientPlayNetworking.send(
                new NpcDialogueActionPayload(
                        npcId,
                        optionId
                )
        );
    }

    public int getNpcId() {
        return npcId;
    }

    public void updateDialogue(
            String message,
            boolean showOptions
    ) {
        this.message = message;
        this.showOptions = showOptions;

        rebuildWidgets();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );

        int x = guiLeft();
        int y = guiTop();

        graphics.text(
                font,
                npcName,
                x + (GUI_WIDTH - font.width(npcName)) / 2,
                y + 20,
                0xFFFFFFFF
        );

        graphics.text(
                font,
                message,
                x + (GUI_WIDTH - font.width(message)) / 2,
                y + 45,
                0xFFE0E0E0
        );
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}