package com.qsage.client.gui.component;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.TextureRegion;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.renderer.RenderPipelines;

public class TabButton extends AbstractWidget {

    private final TextureRegion normal;
    private final TextureRegion hover;
    private final TextureRegion selected;

    private boolean selectedState;

    private final Runnable onPress;

    public TabButton(
            int x,
            int y,
            TextureRegion normal,
            TextureRegion hover,
            TextureRegion selected,
            Runnable onPress
    ) {
        super(
                x,
                y,
                normal.width(),
                normal.height(),
                Component.empty()
        );

        this.normal = normal;
        this.hover = hover;
        this.selected = selected;
        this.onPress = onPress;
    }

    public void setSelected(
            boolean selected
    ) {
        this.selectedState = selected;
    }

    public boolean isSelected() {
        return selectedState;
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        TextureRegion texture;

        if (selectedState) {
            texture = selected;
        } else if (isHovered()) {
            texture = hover;
        } else {
            texture = normal;
        }

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