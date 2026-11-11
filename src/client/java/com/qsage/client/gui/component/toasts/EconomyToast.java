
package com.qsage.client.gui.component.toasts;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;

public final class EconomyToast implements Toast {

    private static final long DURATION_MS = 5000L;

    private static final int TEXT_X = 12;
    private static final int TITLE_Y = 5;
    private static final int MESSAGE_Y = 24;
    private static final int TEXT_RIGHT_MARGIN = 10;

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
    public int width() {
        return GuiAtlas.TOAST_WIDTH;
    }

    @Override
    public int height() {
        return GuiAtlas.TOAST_HEIGHT;
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            Font font,
            long fullyVisibleForMs
    ) {

        // Фон из нашего GUI-атласа
        GuiAtlas.draw(
                graphics,
                GuiStyle.ECONOMY_TOASTS,
                0,
                0
        );

        int maxTextWidth =
                width() - TEXT_X - TEXT_RIGHT_MARGIN;

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
                TEXT_X,
                TITLE_Y,
                type.color(),
                false
        );

        // Текст уведомления
        graphics.text(
                font,
                messageText,
                TEXT_X,
                MESSAGE_Y,
                0xFFFFFFFF,
                false
        );
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
                Math.max(
                        0,
                        maxWidth - font.width(ellipsis)
                )
        ) + ellipsis;
    }
}
