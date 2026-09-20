package com.qsage.client.gui.config.screen;

import com.qsage.config.MarketConfig;
import com.qsage.config.SmartEconomyConfig;
import com.qsage.config.SmartEconomyConfigManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MarketConfigScreen extends Screen {

    private final Screen parent;

    private EditBox priceSpreadField;
    private EditBox trendNeutralThresholdField;

    public MarketConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.smart_economy.config.market"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        SmartEconomyConfig config =
                SmartEconomyConfigManager.get();

        MarketConfig market =
                config.market();

        int centerX = width / 2;

        int labelX = centerX - 140;
        int fieldX = centerX + 20;

        int fieldWidth = 120;

        int startY = 55;
        int rowHeight = 40;

        addRenderableWidget(
                new StringWidget(
                        labelX,
                        startY + 3,
                        150,
                        20,
                        Component.translatable(
                                "gui.smart_economy.config.price_spread"
                        ),
                        font
                )
        );

        priceSpreadField = createField(
                fieldX,
                startY,
                fieldWidth,
                market.priceSpread()
        );

        addRenderableWidget(
                new StringWidget(
                        labelX,
                        startY + rowHeight + 3,
                        150,
                        20,
                        Component.translatable(
                                "gui.smart_economy.config.trend_neutral_threshold"
                        ),
                        font
                )
        );

        trendNeutralThresholdField = createField(
                fieldX,
                startY + rowHeight,
                fieldWidth,
                market.trendNeutralThreshold()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.reset"
                        ),
                        button -> resetFields()
                ).bounds(
                        centerX - 155,
                        height - 30,
                        90,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.cancel"
                        ),
                        button -> onClose()
                ).bounds(
                        centerX - 50,
                        height - 30,
                        90,
                        20
                ).build()
        );

        addRenderableWidget(
                Button.builder(
                        Component.translatable(
                                "gui.smart_economy.config.done"
                        ),
                        button -> saveAndClose()
                ).bounds(
                        centerX + 55,
                        height - 30,
                        90,
                        20
                ).build()
        );
    }

    private EditBox createField(
            int x,
            int y,
            int width,
            double value
    ) {
        EditBox field = new EditBox(
                font,
                x,
                y,
                width,
                20,
                Component.empty()
        );

        field.setValue(Double.toString(value));
        field.setMaxLength(16);

        addRenderableWidget(field);

        return field;
    }

    private void resetFields() {
        MarketConfig defaults =
                MarketConfig.defaults();

        priceSpreadField.setValue(
                Double.toString(defaults.priceSpread())
        );

        trendNeutralThresholdField.setValue(
                Double.toString(defaults.trendNeutralThreshold())
        );
    }

    private void saveAndClose() {
        try {
            double priceSpread =
                    Double.parseDouble(
                            priceSpreadField.getValue()
                    );

            double trendNeutralThreshold =
                    Double.parseDouble(
                            trendNeutralThresholdField.getValue()
                    );

            MarketConfig marketConfig =
                    new MarketConfig(
                            priceSpread,
                            trendNeutralThreshold
                    ).validate();

            SmartEconomyConfig current =
                    SmartEconomyConfigManager.get();

            SmartEconomyConfig updated =
                    new SmartEconomyConfig(
                            marketConfig,
                            current.pricing(),
                            current.inflation(),
                            current.trading()
                    ).validate();

            SmartEconomyConfigManager.set(updated);
            SmartEconomyConfigManager.save();

            minecraft.gui.setScreen(parent);

        } catch (NumberFormatException ignored) {
        }
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