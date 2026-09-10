package com.qsage.client.gui.screen;

import com.qsage.client.market.ClientExchange;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.component.BalanceWidget;
import com.qsage.client.gui.component.MarketLotWidget;
import com.qsage.client.gui.component.TabButton;
import com.qsage.economy.market.network.ExchangeQuotePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TestEconomyScreen extends EconomyScreen {

    public TestEconomyScreen() {
        super(Component.translatable(
                "gui.smart_economy.market.title"
        ));
    }

    @Override
    protected void initEconomyScreen() {

        /*
         * ========================================================
         * Balance
         * ========================================================
         */

        addGuiComponent(
                new BalanceWidget(),
                220,
                -18
        );


        /*
         * ========================================================
         * Tabs
         * ========================================================
         */

        TabButton tab1 = new TabButton(
                guiLeft() + 8,
                guiTop() + 30,
                GuiStyle.TAB,
                GuiStyle.TAB_HOVER,
                GuiStyle.TAB_SELECTED,
                () -> {
                    // Пока ничего не делаем
                }
        );

        tab1.setSelected(true);

        addRenderableWidget(tab1);


        TabButton tab2 = new TabButton(
                guiLeft() + 8,
                guiTop() + 70,
                GuiStyle.TAB,
                GuiStyle.TAB_HOVER,
                GuiStyle.TAB_SELECTED,
                () -> {
                    // Пока ничего не делаем
                }
        );

        addRenderableWidget(tab2);


        TabButton tab3 = new TabButton(
                guiLeft() + 8,
                guiTop() + 110,
                GuiStyle.TAB,
                GuiStyle.TAB_HOVER,
                GuiStyle.TAB_SELECTED,
                () -> {
                    // Пока ничего не делаем
                }
        );

        addRenderableWidget(tab3);


        /*
         * ========================================================
         * Exchange lots
         * ========================================================
         */

        createExchangeLots();
    }


    private void createExchangeLots() {

        System.out.println(
                "Client exchange quotes: "
                        + ClientExchange.getAll().size()
        );

        Map<Identifier, ExchangeQuotePayload> quotes =
                ClientExchange.getAll();

        List<ExchangeQuotePayload> sortedQuotes =
                new ArrayList<>(quotes.values());

        sortedQuotes.sort(
                Comparator.comparing(
                        quote -> quote.itemId().toString()
                )
        );

        int index = 0;

        for (ExchangeQuotePayload quote : sortedQuotes) {

            Item item = BuiltInRegistries.ITEM
                    .getOptional(quote.itemId())
                    .orElse(null);

            if (item == null || item == Items.AIR) {
                continue;
            }

            ItemStack stack = item.getDefaultInstance();

            Component name = stack.getHoverName();

            MarketLotWidget lot = new MarketLotWidget(
                    stack,
                    name,
                    quote.buyPrice(),
                    quote.availableQuantity(),
                    MarketLotWidget.Trend.NEUTRAL,
                    GuiStyle.MARKET_LOT,
                    () -> Minecraft.getInstance().gui.setScreen(
                            new MarketTradeScreen(
                                    this,
                                    stack
                            )
                    )
            );

            addGuiComponent(
                    lot,
                    18,
                    16 + index * 32
            );

            index++;
        }
    }
}