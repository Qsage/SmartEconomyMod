package com.qsage.client.gui.component;

import com.qsage.client.economy.MoneyFormatter;
import com.qsage.client.economy.NumberFormatter;
import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.TextureRegion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MarketLotWidget extends GuiComponent {

    public enum Trend {
        UP,
        DOWN,
        NEUTRAL
    }

    private final ItemStack item;
    private final Component name;
    private final long price;
    private final long stock;
    private final Trend trend;
    private final TextureRegion background;
    private final Runnable onClick;

    public MarketLotWidget(
            ItemStack item,
            Component name,
            long price,
            long stock,
            Trend trend,
            TextureRegion background,
            Runnable onClick
    ) {
        super(0, 0);

        this.item = item.copy();
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.trend = trend;
        this.background = background;
        this.onClick = onClick;
    }


    @Override
    public int getWidth() {
        return background.width();
    }

    @Override
    public int getHeight() {
        return background.height();
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

        /*
         * ============================================================
         * Background
         * ============================================================
         */

        GuiAtlas.draw(
                graphics,
                background,
                x,
                y
        );


        /*
         * ============================================================
         * ItemStack
         * ============================================================
         */

        graphics.fakeItem(
                item,
                x + GuiStyle.LOT_ITEM_X,
                y + GuiStyle.LOT_ITEM_Y
        );


        /*
         * ============================================================
         * Name
         * ============================================================
         */

        graphics.text(
                font,
                name,
                x + GuiStyle.LOT_NAME_X,
                y + GuiStyle.LOT_NAME_Y,
                0xFFFFFFFF,
                true
        );


        /*
         * ============================================================
         * Trend
         * ============================================================
         */

        TextureRegion trendTexture = switch (trend) {
            case UP -> GuiStyle.TREND_UP;
            case DOWN -> GuiStyle.TREND_DOWN;
            case NEUTRAL -> GuiStyle.TREND_NEUTRAL;
        };

        int trendX =
                x
                        + getWidth()
                        - GuiStyle.LOT_TREND_MARGIN_RIGHT
                        - trendTexture.width();

        GuiAtlas.draw(
                graphics,
                trendTexture,
                trendX,
                y + GuiStyle.LOT_TREND_Y
        );



        Component quantityComponent = Component.translatable(
                "gui.smart_economy.market.quantity",
                NumberFormatter.formatCompact(stock)
        );

        Component priceComponent = Component.literal(
                MoneyFormatter.format(price)
        );



        int quantityWidth = font.width(quantityComponent);
        int priceWidth = font.width(priceComponent);

        /*
         * Весь блок заканчивается перед trend.
         */
        int infoRight =
                trendX
                        - GuiStyle.LOT_QUANTITY_MARGIN_RIGHT;

        /*
         * Цена находится справа.
         */
        int priceX =
                infoRight
                        - priceWidth;

        /*
         * Количество находится слева от цены.
         */
        int quantityX =
                priceX
                        - GuiStyle.LOT_PRICE_GAP
                        - quantityWidth;

        boolean quantityHovered =
                mouseX >= quantityX
                        && mouseX < quantityX + quantityWidth
                        && mouseY >= y + GuiStyle.LOT_QUANTITY_Y
                        && mouseY < y + GuiStyle.LOT_QUANTITY_Y + font.lineHeight;
        /*
         * Количество
         */
        graphics.text(
                font,
                quantityComponent,
                quantityX,
                y + GuiStyle.LOT_QUANTITY_Y,
                0xFFFFFFFF,
                true
        );

        if (quantityHovered) {
            Component quantityTooltip = Component.literal(
                    NumberFormatter.formatFull(stock) + " шт."
            ).withStyle(style -> style.withColor(0xFFD700));

            graphics.setTooltipForNextFrame(
                    font,
                    quantityTooltip,
                    mouseX,
                    mouseY
            );
        }


        /*
         * Цена
         */
        graphics.text(
                font,
                priceComponent,
                priceX,
                y + GuiStyle.LOT_PRICE_Y,
                0xffffd700,
                true
        );


    }

    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 0 && contains(mouseX, mouseY)) {
            if (onClick != null) {
                onClick.run();
            }

            return true;
        }

        return false;
    }

    private boolean isTrendHovered(
            double mouseX,
            double mouseY
    ) {
        int x = getX()
                + getWidth()
                - GuiStyle.LOT_TREND_MARGIN_RIGHT
                - GuiStyle.TREND_UP.width();

        int y = getY() + GuiStyle.LOT_TREND_Y;

        return mouseX >= x
                && mouseX < x + GuiStyle.TREND_UP.width()
                && mouseY >= y
                && mouseY < y + GuiStyle.TREND_UP.height();
    }


}