package com.qsage.client.gui;

import com.qsage.SmartEconomy;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;

public final class GuiAtlas {

    public static final String PATH =
            "textures/gui/gui_atlas.png";

    public static final net.minecraft.resources.Identifier TEXTURE =
            SmartEconomy.id(PATH);

    /**
     * Размер всего PNG-атласа.
     */
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

    private GuiAtlas() {
    }

    public static void draw(
            GuiGraphicsExtractor graphics,
            TextureRegion region,
            int x,
            int y
    ) {
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                x,
                y,
                region.u(),
                region.v(),
                region.width(),
                region.height(),
                region.width(),
                region.height(),
                WIDTH,
                HEIGHT
        );
    }
}