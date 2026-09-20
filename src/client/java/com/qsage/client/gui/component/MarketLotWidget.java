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

import static com.qsage.client.gui.GuiStyle.LOT_NAME_MAX_LENGTH;

public class MarketLotWidget extends GuiComponent {

    public enum Trend {
        UP,
        DOWN,
        NEUTRAL
    }

    /*
     * ============================================================
     * Marquee settings
     * ============================================================
     */

    private static final long MARQUEE_DELAY_MS = 700L;
    private static final long MARQUEE_STEP_MS = 250L;
    private static final long MARQUEE_END_PAUSE_MS = 900L;

    private final ItemStack item;
    private final Component name;
    private final long price;
    private final long stock;
    private final Trend trend;
    private final TextureRegion background;
    private final Runnable onClick;

    private long hoverStartTime = 0L;
    private long lastMarqueeStep = 0L;

    private int marqueeOffset = 0;
    private boolean marqueeAtEnd = false;

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

        String fullName = name.getString();

        int maxNameLength =
                GuiStyle.LOT_NAME_MAX_LENGTH;

        int nameX =
                x + GuiStyle.LOT_NAME_X;

        int nameY =
                y + GuiStyle.LOT_NAME_Y;

        boolean nameHovered =
                mouseX >= nameX
                        && mouseX < nameX + GuiStyle.LOT_NAME_AREA_WIDTH
                        && mouseY >= nameY
                        && mouseY < nameY + font.lineHeight;

        boolean tooLong =
                fullName.length() > maxNameLength;

        String displayName;

        if (!tooLong) {

            displayName = fullName;

        } else {

            if (nameHovered) {
                updateMarquee();
                displayName = getMarqueeText(fullName);
            } else {
                displayName = truncateName(fullName);
            }
        }

        Component displayComponent =
                Component.literal(displayName)
                        .withStyle(name.getStyle());

        graphics.text(
                font,
                displayComponent,
                nameX,
                nameY,
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

        /*
         * ============================================================
         * Quantity / Price
         * ============================================================
         */

        Component quantityComponent =
                Component.translatable(
                        "gui.smart_economy.market.quantity",
                        NumberFormatter.formatCompact(stock)
                );

        Component priceComponent =
                Component.literal(
                        MoneyFormatter.format(price)
                );

        int quantityWidth =
                font.width(quantityComponent);

        int priceWidth =
                font.width(priceComponent);

        int infoRight =
                trendX
                        - GuiStyle.LOT_QUANTITY_MARGIN_RIGHT;

        int priceX =
                infoRight
                        - priceWidth;

        int quantityX =
                priceX
                        - GuiStyle.LOT_PRICE_GAP
                        - quantityWidth;

        boolean quantityHovered =
                mouseX >= quantityX
                        && mouseX < quantityX + quantityWidth
                        && mouseY >= y + GuiStyle.LOT_QUANTITY_Y
                        && mouseY < y + GuiStyle.LOT_QUANTITY_Y
                        + font.lineHeight;

        graphics.text(
                font,
                quantityComponent,
                quantityX,
                y + GuiStyle.LOT_QUANTITY_Y,
                0xFFFFFFFF,
                true
        );

        if (quantityHovered) {

            Component quantityTooltip =
                    Component.literal(
                            NumberFormatter.formatFull(stock)
                                    + " шт."
                    ).withStyle(
                            style -> style.withColor(0xFFD700)
                    );

            graphics.setTooltipForNextFrame(
                    font,
                    quantityTooltip,
                    mouseX,
                    mouseY
            );
        }

        graphics.text(
                font,
                priceComponent,
                priceX,
                y + GuiStyle.LOT_PRICE_Y,
                0xFFFFD700,
                true
        );
    }

    /*
     * ============================================================
     * Marquee
     * ============================================================
     */

    private void updateMarquee() {

        long now =
                System.currentTimeMillis();

        if (hoverStartTime == 0L) {
            hoverStartTime = now;
            lastMarqueeStep = now;
            return;
        }

        long hoveredFor =
                now - hoverStartTime;

        if (hoveredFor < MARQUEE_DELAY_MS) {
            return;
        }

        if (marqueeAtEnd) {

            if (now - lastMarqueeStep >= MARQUEE_END_PAUSE_MS) {
                marqueeOffset = 0;
                marqueeAtEnd = false;
                lastMarqueeStep = now;
            }

            return;
        }

        if (now - lastMarqueeStep < MARQUEE_STEP_MS) {
            return;
        }

        marqueeOffset++;
        lastMarqueeStep = now;
    }

    private void resetMarquee() {
        hoverStartTime = 0L;
        lastMarqueeStep = 0L;
        marqueeOffset = 0;
        marqueeAtEnd = false;
    }

    private String truncateName(String text) {

        int maxLength =
                GuiStyle.LOT_NAME_MAX_LENGTH;

        if (text.length() <= maxLength) {
            return text;
        }

        int visibleLength =
                Math.max(
                        0,
                        maxLength
                );

        return text.substring(
                0,
                visibleLength
        ) + "...";
    }

    private String getMarqueeText(String text) {

        int maxLength =
                GuiStyle.LOT_NAME_MAX_LENGTH;

        if (marqueeOffset >= text.length()) {
            marqueeAtEnd = true;
            return truncateName(text);
        }

        int end =
                Math.min(
                        marqueeOffset + maxLength,
                        text.length()
                );

        String visible =
                text.substring(
                        marqueeOffset,
                        end
                );

        if (end >= text.length()) {
            marqueeAtEnd = true;
        }

        return visible;
    }

    /*
     * ============================================================
     * Mouse
     * ============================================================
     */

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
        int x =
                getX()
                        + getWidth()
                        - GuiStyle.LOT_TREND_MARGIN_RIGHT
                        - GuiStyle.TREND_UP.width();

        int y =
                getY()
                        + GuiStyle.LOT_TREND_Y;

        return mouseX >= x
                && mouseX < x + GuiStyle.TREND_UP.width()
                && mouseY >= y
                && mouseY < y + GuiStyle.TREND_UP.height();
    }
}