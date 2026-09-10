package com.qsage.client.gui.component;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.TextureRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class ActionButton extends AbstractWidget {

    private final TextureRegion normal;
    private final TextureRegion hover;
    private final Runnable onPress;

    public ActionButton(
            int x,
            int y,
            TextureRegion normal,
            TextureRegion hover,
            Component message,
            Runnable onPress
    ) {
        super(
                x,
                y,
                normal.width(),
                normal.height(),
                message
        );

        this.normal = normal;
        this.hover = hover;
        this.onPress = onPress;
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        TextureRegion texture =
                isHovered() ? hover : normal;

        // Фон кнопки
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                GuiAtlas.TEXTURE,
                getX(),
                getY(),
                texture.u(),
                texture.v(),
                texture.width(),
                texture.height(),
                texture.width(),
                texture.height(),
                GuiAtlas.WIDTH,
                GuiAtlas.HEIGHT
        );

        // Текст кнопки
        Font font = Minecraft.getInstance().font;

        int textX =
                getX()
                        + (getWidth() - font.width(getMessage())) / 2;

        int textY =
                getY()
                        + (getHeight() - font.lineHeight) / 2;

        graphics.text(
                font,
                getMessage(),
                textX,
                textY,
                0xFFFFFFFF,
                true
        );
    }

    @Override
    public void onClick(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (onPress != null) {
            onPress.run();
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        output.add(
                net.minecraft.client.gui.narration.NarratedElementType.TITLE,
                getMessage()
        );
    }
}