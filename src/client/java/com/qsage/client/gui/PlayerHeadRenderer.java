
package com.qsage.client.gui;

import com.qsage.client.player.ClientPlayerSkins;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

import java.util.UUID;

public final class PlayerHeadRenderer {

    private static final int SKIN_WIDTH = 64;
    private static final int SKIN_HEIGHT = 64;

    private PlayerHeadRenderer() {
    }

    public static void render(
            GuiGraphicsExtractor graphics,
            UUID uuid,
            int x,
            int y,
            int size
    ) {
        Identifier skin = ClientPlayerSkins.getSkin(uuid);

        if (skin == null) {
            return;
        }

        // Основной слой лица
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                skin,
                x,
                y,
                8,
                8,
                size,
                size,
                8,
                8,
                SKIN_WIDTH,
                SKIN_HEIGHT
        );

        // Внешний слой головы (волосы, шляпа и т. п.)
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                skin,
                x,
                y,
                40,
                8,
                size,
                size,
                8,
                8,
                SKIN_WIDTH,
                SKIN_HEIGHT
        );
    }
}
