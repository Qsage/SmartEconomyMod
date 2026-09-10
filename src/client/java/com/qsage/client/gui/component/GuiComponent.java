package com.qsage.client.gui.component;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class GuiComponent {

    private int x;
    private int y;

    protected GuiComponent(
            int x,
            int y
    ) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(
            int x,
            int y
    ) {
        this.x = x;
        this.y = y;
    }

    public abstract int getWidth();

    public abstract int getHeight();

    public boolean contains(
            double mouseX,
            double mouseY
    ) {
        return mouseX >= x
                && mouseX < x + getWidth()
                && mouseY >= y
                && mouseY < y + getHeight();
    }



    public abstract void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    );
}