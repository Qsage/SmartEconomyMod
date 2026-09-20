package com.qsage.client.gui.component;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.TextureRegion;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class TabButton extends AbstractWidget {

    private final TextureRegion normal;
    private final TextureRegion hover;
    private final TextureRegion selected;

    private TextureRegion icon;
    private ItemStack itemIcon;

    private boolean selectedState;

    private final Runnable onPress;

    /*
     * Текущий вариант для текстурной иконки.
     */
    public TabButton(
            int x,
            int y,
            TextureRegion normal,
            TextureRegion hover,
            TextureRegion selected,
            Component tooltip,
            TextureRegion icon,
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
        this.icon = icon;
        this.itemIcon = null;
        this.onPress = onPress;

        setTooltip(Tooltip.create(tooltip));
    }

    /*
     * Новый вариант для ItemStack.
     */
    public TabButton(
            int x,
            int y,
            TextureRegion normal,
            TextureRegion hover,
            TextureRegion selected,
            Component tooltip,
            ItemStack itemIcon,
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
        this.icon = null;
        this.itemIcon = itemIcon.copy();
        this.onPress = onPress;

        setTooltip(Tooltip.create(tooltip));
    }

    /*
     * Старый вариант без иконки.
     */
    public TabButton(
            int x,
            int y,
            TextureRegion normal,
            TextureRegion hover,
            TextureRegion selected,
            Component tooltip,
            Runnable onPress
    ) {
        this(
                x,
                y,
                normal,
                hover,
                selected,
                tooltip,
                (TextureRegion) null,
                onPress
        );
    }

    public void setSelected(boolean selected) {
        this.selectedState = selected;
    }

    public boolean isSelected() {
        return selectedState;
    }

    public void setIcon(TextureRegion icon) {
        this.icon = icon;
        this.itemIcon = null;
    }

    public void setItemIcon(ItemStack itemIcon) {
        this.itemIcon =
                itemIcon == null
                        ? null
                        : itemIcon.copy();

        this.icon = null;
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

        /*
         * ============================================================
         * Background
         * ============================================================
         */

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

        /*
         * ============================================================
         * Item icon
         * ============================================================
         */

        if (itemIcon != null) {

            int itemX =
                    getX()
                            + (getWidth() - 16) / 2 - 2;

            int itemY =
                    getY()
                            + (getHeight() - 16) / 2 - 1;

            graphics.fakeItem(
                    itemIcon,
                    itemX,
                    itemY
            );
        }

        /*
         * ============================================================
         * Texture icon
         * ============================================================
         */

        if (icon != null) {

            int iconX =
                    getX()
                            + (getWidth() - icon.width()) / 2;

            int iconY =
                    getY()
                            + (getHeight() - icon.height()) / 2;

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    GuiAtlas.TEXTURE,
                    iconX,
                    iconY,
                    icon.u(),
                    icon.v(),
                    icon.width(),
                    icon.height(),
                    icon.width(),
                    icon.height(),
                    GuiAtlas.WIDTH,
                    GuiAtlas.HEIGHT
            );
        }
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