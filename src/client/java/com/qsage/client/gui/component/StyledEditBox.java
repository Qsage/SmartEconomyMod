package com.qsage.client.gui.component;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.TextureRegion;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.Font;

public class StyledEditBox extends EditBox {

    private final TextureRegion background;

    public StyledEditBox(
            Font font,
            int x,
            int y,
            int width,
            int height,
            TextureRegion background
    ) {
        super(
                font,
                x,
                y,
                width,
                height,
                net.minecraft.network.chat.Component.empty()
        );

        this.background = background;
    }

    @Override
    public void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        /*
         * Наш фон
         */
        GuiAtlas.draw(
                graphics,
                background,
                getX(),
                getY()
        );

        /*
         * Рисуем содержимое EditBox:
         * текст, курсор и выделение.
         *
         * Здесь intentionally не вызываем обычный
         * background EditBox.
         */
        super.extractWidgetRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
    }
}