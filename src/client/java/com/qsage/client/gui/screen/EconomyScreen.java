package com.qsage.client.gui.screen;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.component.GuiComponent;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public abstract class EconomyScreen extends Screen {

    /**
     * Логический размер нашего GUI.
     */
    protected static final int GUI_WIDTH = 320;
    protected static final int GUI_HEIGHT = 220;

    private final List<GuiComponent> components =
            new ArrayList<>();

    protected EconomyScreen(
            Component title
    ) {
        super(title);
    }

    // =========================================================
    // INIT
    // =========================================================

    @Override
    protected void init() {
        super.init();

        components.clear();

        initEconomyScreen();
    }

    /**
     * Здесь дочерний Screen создаёт свои компоненты.
     */
    protected abstract void initEconomyScreen();

    // =========================================================
    // GUI POSITION
    // =========================================================

    protected int guiLeft() {
        return (this.width - GUI_WIDTH) / 2;
    }

    protected int guiTop() {
        return (this.height - GUI_HEIGHT) / 2;
    }

    // =========================================================
    // CUSTOM COMPONENTS
    // =========================================================

    protected <T extends GuiComponent> T addGuiComponent(
            T component,
            int x,
            int y
    ) {
        component.setPosition(
                guiLeft() + x,
                guiTop() + y
        );

        components.add(component);

        return component;
    }

    protected List<GuiComponent> getComponents() {
        return components;
    }

    // =========================================================
    // VANILLA WIDGETS / BUTTONS
    // =========================================================

    protected <T extends AbstractWidget> T addGuiWidget(
            T widget,
            int x,
            int y
    ) {
        widget.setX(
                guiLeft() + x
        );

        widget.setY(
                guiTop() + y
        );

        addRenderableWidget(widget);

        return widget;
    }

    // =========================================================
    // BACKGROUND
    // =========================================================

    protected void renderEconomyBackground(
            GuiGraphicsExtractor graphics
    ) {
        GuiAtlas.draw(
                graphics,
                GuiStyle.BOOK,
                guiLeft(),
                guiTop()
        );
    }

    // =========================================================
    // RENDER
    // =========================================================

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

        // Фон книги
        renderEconomyBackground(graphics);

        // Наши декоративные компоненты
        for (GuiComponent component : components) {
            component.render(
                    graphics,
                    mouseX,
                    mouseY,
                    delta
            );
        }
    }

    // =========================================================
    // ESC
    // =========================================================

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}