package com.qsage.client.gui.component;

import com.qsage.client.economy.ClientEconomy;
import com.qsage.client.economy.MoneyFormatter;
import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class BalanceWidget extends GuiComponent {

    public BalanceWidget() {
        super(0, 0);
    }

    @Override
    public int getWidth() {
        return GuiStyle.BALANCE.width();
    }

    @Override
    public int getHeight() {
        return GuiStyle.BALANCE.height();
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        int x = getX();
        int y = getY();

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        // Фон
        GuiAtlas.draw(
                graphics,
                GuiStyle.BALANCE,
                x,
                y
        );

        long balance = ClientEconomy.getAvailable();

        // Компактное отображение
        String balanceText = MoneyFormatter.format(balance);
        Component balanceComponent = Component.literal(balanceText);

        int textX =
                x
                        + getWidth()
                        - GuiStyle.BALANCE_TEXT_MARGIN_RIGHT
                        - font.width(balanceComponent);

        graphics.text(
                font,
                balanceComponent,
                textX,
                y + GuiStyle.BALANCE_TEXT_Y,
                0xffffd700,
                true
        );

        // Tooltip
        if (contains(mouseX, mouseY)) {
            Component tooltip = Component.literal(
                    MoneyFormatter.formatFull(balance)
            ).withStyle(style -> style.withColor(0xFFD700));

            graphics.setTooltipForNextFrame(
                    font,
                    tooltip,
                    mouseX,
                    mouseY
            );
        }
    }
}