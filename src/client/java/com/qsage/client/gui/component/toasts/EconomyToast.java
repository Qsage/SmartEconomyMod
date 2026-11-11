
package com.qsage.client.gui.component.toasts;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public final class EconomyToast implements Toast {

    private static final long DURATION_MS = 5000L;

    public enum Type {
        SUCCESS(0xFF35E06B),
        ERROR(0xFFFF5555),
        INFO(0xFF55AAFF);

        private final int color;

        Type(int color) {
            this.color = color;
        }

        public int color() {
            return color;
        }
    }

    private final Type type;
    private final Component title;
    private final Component message;

    private Visibility visibility = Visibility.SHOW;

    public EconomyToast(
            Type type,
            Component title,
            Component message
    ) {
        this.type = type;
        this.title = title;
        this.message = message;
    }


    private static String truncate(
            Font font,
            String text,
            int maxWidth
    ) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "…";

        return font.plainSubstrByWidth(
                text,
                Math.max(0, maxWidth - font.width(ellipsis))
        ) + ellipsis;
    }


    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(
            ToastManager manager,
            long fullyVisibleForMs
    ) {
        if (fullyVisibleForMs >= DURATION_MS) {
            visibility = Visibility.HIDE;
        }
    }


    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            long fullyVisibleForMs
    ) {
        // Фон уведомления
        graphics.fill(
                0, 0,
                width(), height(),
                0xEE202020
        );

        // Цветовая полоса слева
        graphics.fill(
                0, 0,
                3, height(),
                type.color()
        );

        // Максимальная ширина текста
        int maxTextWidth = width() - 20;

        String titleText = truncate(
                font,
                title.getString(),
                maxTextWidth
        );

        String messageText = truncate(
                font,
                message.getString(),
                maxTextWidth
        );

        // Заголовок
        graphics.text(
                font,
                titleText,
                10, 5,
                type.color(),
                false
        );

        // Описание
        graphics.text(
                font,
                messageText,
                10, 18,
                0xFFFFFFFF,
                false
        );
    }

}
