package com.qsage.client.gui.screen;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.component.ActionButton;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class MarketTradeScreen extends net.minecraft.client.gui.screens.Screen {

    private static final int WINDOW_WIDTH = 174;
    private static final int WINDOW_HEIGHT = 174;

    private final net.minecraft.client.gui.screens.Screen parent;
    private final ItemStack item;

    private EditBox amountField;

    public MarketTradeScreen(
            net.minecraft.client.gui.screens.Screen parent,
            ItemStack item
    ) {
        super(Component.empty());

        this.parent = parent;
        this.item = item.copy();
    }

    @Override
    protected void init() {
        super.init();

        int left = (this.width - WINDOW_WIDTH) / 2;
        int top = (this.height - WINDOW_HEIGHT) / 2;

        /*
         * ========================================================
         * Amount field
         * ========================================================
         */

        int amountWidth = GuiStyle.TRADE_AMOUNT_FIELD.width();
        int amountHeight = GuiStyle.TRADE_AMOUNT_FIELD.height();

        int amountX = getAmountX(left);
        int amountY = getAmountY(top);

        amountField = new EditBox(
                this.font,
                amountX,
                amountY,
                amountWidth,
                amountHeight,
                Component.translatable(
                        "gui.smart_economy.market.amount"
                )
        );

        // Убираем стандартную рамку EditBox.
        amountField.setBordered(false);

        // Максимальная длина ввода.
        amountField.setMaxLength(12);

        // Начальное количество.
        amountField.setValue("1");

        addRenderableWidget(amountField);


        /*
         * ========================================================
         * BUY
         * ========================================================
         */

        int buttonWidth = GuiStyle.BUTTON_BUY.width();

        int totalButtonsWidth =
                buttonWidth * 2
                        + GuiStyle.TRADE_BUTTON_GAP;

        int buttonsLeft =
                left
                        + (WINDOW_WIDTH - totalButtonsWidth) / 2;

        int buttonY =
                top
                        + GuiStyle.TRADE_BUTTON_Y;

        ActionButton buyButton = new ActionButton(
                buttonsLeft,
                buttonY,
                GuiStyle.BUTTON_BUY,
                GuiStyle.BUTTON_BUY_HOVER,
                Component.translatable(
                        "gui.smart_economy.market.buy"
                ),
                this::onBuy
        );

        addRenderableWidget(buyButton);


        /*
         * ========================================================
         * SELL
         * ========================================================
         */

        int sellX =
                buttonsLeft
                        + buttonWidth
                        + GuiStyle.TRADE_BUTTON_GAP;

        ActionButton sellButton = new ActionButton(
                sellX,
                buttonY,
                GuiStyle.BUTTON_SELL,
                GuiStyle.BUTTON_SELL_HOVER,
                Component.translatable(
                        "gui.smart_economy.market.sell"
                ),
                this::onSell
        );

        addRenderableWidget(sellButton);


        /*
         * ========================================================
         * Focus amount field
         * ========================================================
         */

        setInitialFocus(amountField);
    }


    /*
     * ============================================================
     * Amount position
     * ============================================================
     */

    private int getAmountX(int left) {
        return left
                + (WINDOW_WIDTH
                - GuiStyle.TRADE_AMOUNT_FIELD.width()) / 2;
    }

    private int getAmountY(int top) {
        return top + GuiStyle.TRADE_AMOUNT_Y;
    }


    /*
     * ============================================================
     * Amount
     * ============================================================
     */

    private long getAmount() {
        String text = amountField.getValue();

        if (text.isBlank()) {
            return 0;
        }

        if (!text.chars().allMatch(Character::isDigit)) {
            return 0;
        }

        try {
            return Long.parseLong(text);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }


    /*
     * ============================================================
     * BUY
     * ============================================================
     */

    private void onBuy() {

        long amount = getAmount();

        if (amount <= 0) {
            return;
        }

        /*
         * Позже здесь будет:
         *
         * ExchangeNetworking.sendBuy(
         *     itemId,
         *     amount
         * );
         */
    }


    /*
     * ============================================================
     * SELL
     * ============================================================
     */

    private void onSell() {

        long amount = getAmount();

        if (amount <= 0) {
            return;
        }

        /*
         * Позже здесь будет:
         *
         * ExchangeNetworking.sendSell(
         *     itemId,
         *     amount
         * );
         */
    }


    /*
     * ============================================================
     * Render
     * ============================================================
     */

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {

        int left = (this.width - WINDOW_WIDTH) / 2;
        int top = (this.height - WINDOW_HEIGHT) / 2;


        /*
         * ========================================================
         * Background screen
         * ========================================================
         *
         * Рисуем предыдущий экран под окном.
         */

        if (parent != null) {
            parent.extractRenderState(
                    graphics,
                    mouseX,
                    mouseY,
                    delta
            );
        }


        /*
         * ========================================================
         * Dark overlay
         * ========================================================
         */

        graphics.fill(
                0,
                0,
                this.width,
                this.height,
                0x99000000
        );


        /*
         * ========================================================
         * Trade window
         * ========================================================
         */

        GuiAtlas.draw(
                graphics,
                GuiStyle.TRADE_WINDOW,
                left,
                top
        );


        /*
         * ========================================================
         * Item
         * ========================================================
         */

        int centerX =
                left
                        + WINDOW_WIDTH / 2;

        int itemX = centerX - 8;
        int itemY = top + GuiStyle.TRADE_ITEM_Y;

        graphics.fakeItem(
                item,
                itemX,
                top + GuiStyle.TRADE_ITEM_Y
        );

        /*
         * ========================================================
         * Item name
         * ========================================================
         */

        Component itemName = item.getHoverName();

        int nameX =
                centerX
                        - this.font.width(itemName) / 2;

        graphics.text(
                this.font,
                itemName,
                nameX,
                top + GuiStyle.TRADE_NAME_Y,
                0xFFFFFFFF,
                true
        );


        /*
         * ========================================================
         * Amount field background
         * ========================================================
         */

        int amountWidth =
                GuiStyle.TRADE_AMOUNT_FIELD.width();

        int amountHeight =
                GuiStyle.TRADE_AMOUNT_FIELD.height();

        int amountX =
                left
                        + (WINDOW_WIDTH - amountWidth) / 2;

        int amountY =
                top
                        + GuiStyle.TRADE_AMOUNT_Y;

        GuiAtlas.draw(
                graphics,
                GuiStyle.TRADE_AMOUNT_FIELD,
                amountX,
                amountY
        );

        /*
         * ========================================================
         * Widgets
         * ========================================================
         *
         */

        int buttonWidth = GuiStyle.BUTTON_BUY.width();

        int totalButtonWidth =
                buttonWidth * 2
                        + GuiStyle.TRADE_BUTTON_GAP;

        int buttonsLeft =
                left
                        + (WINDOW_WIDTH - totalButtonWidth) / 2;

        int buttonY =
                top
                        + GuiStyle.TRADE_BUTTON_Y;


        ActionButton buyButton = new ActionButton(
                buttonsLeft,
                buttonY,
                GuiStyle.BUTTON_BUY,
                GuiStyle.BUTTON_BUY_HOVER,
                Component.translatable(
                        "gui.smart_economy.market.buy"
                ),
                this::onBuy
        );

        ActionButton sellButton = new ActionButton(
                buttonsLeft
                        + buttonWidth
                        + GuiStyle.TRADE_BUTTON_GAP,
                buttonY,
                GuiStyle.BUTTON_SELL,
                GuiStyle.BUTTON_SELL_HOVER,
                Component.translatable(
                        "gui.smart_economy.market.sell"
                ),
                this::onSell
        );

        addRenderableWidget(sellButton);

        addRenderableWidget(buyButton);

        super.extractRenderState(
                graphics,
                mouseX,
                mouseY,
                delta
        );
    }


    /*
     * ============================================================
     * Close
     * ============================================================
     */

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().gui.setScreen(parent);
    }
}