package com.qsage.client.gui.screen;

import com.qsage.client.gui.config.screen.InflationConfigScreen;
import com.qsage.client.gui.config.screen.MarketConfigScreen;
import com.qsage.client.gui.config.screen.PricingConfigScreen;
import com.qsage.client.gui.config.screen.TradingConfigScreen;
import com.qsage.config.SmartEconomyConfigManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class EconomyConfigScreen extends Screen {

    private final Screen parent;

    public EconomyConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.smart_economy.config.title"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = width / 2;

        int buttonWidth = 200;
        int buttonHeight = 20;

        int x = centerX - buttonWidth / 2;
        int y = 50;
        int spacing = 28;

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.market"
                        ),
                        button -> openMarketConfig()
                ).bounds(
                        x,
                        y,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.pricing"
                        ),
                        button -> openPricingConfig()
                ).bounds(
                        x,
                        y + spacing,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.inflation"
                        ),
                        button -> openInflationConfig()
                ).bounds(
                        x,
                        y + spacing * 2,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.trading"
                        ),
                        button -> openTradingConfig()
                ).bounds(
                        x,
                        y + spacing * 3,
                        buttonWidth,
                        buttonHeight
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.reset"
                        ),
                        button -> resetConfig()
                ).bounds(
                        centerX - 105,
                        height - 30,
                        100,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.done"
                        ),
                        button -> onClose()
                ).bounds(
                        centerX + 5,
                        height - 30,
                        100,
                        20
                ).build()
        );
    }

    private void openTradingConfig() {
        minecraft.gui.setScreen(
                new TradingConfigScreen(this)
        );
    }

    private void openInflationConfig() {
        minecraft.gui.setScreen(
                new InflationConfigScreen(this)
        );
    }

    private void resetConfig() {
        SmartEconomyConfigManager.reset();
        SmartEconomyConfigManager.save();

        minecraft.gui.setScreen(
                new EconomyConfigScreen(parent)
        );
    }

    private void openMarketConfig() {
        minecraft.gui.setScreen(
                new MarketConfigScreen(this)
        );
    }

    private void openPricingConfig() {
        minecraft.gui.setScreen(
                new PricingConfigScreen(this)
        );
    }

    @Override
    public void onClose() {
        minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}