package com.qsage.client.gui.config.screen;

import com.qsage.config.PricingConfig;
import com.qsage.config.SmartEconomyConfig;
import com.qsage.config.SmartEconomyConfigManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PricingConfigScreen extends Screen {

    private final Screen parent;

    private EditBox smoothingFactorField;
    private EditBox maxPriceDeviationField;

    public PricingConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.smart_economy.config.pricing"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        SmartEconomyConfig config =
                SmartEconomyConfigManager.get();

        PricingConfig pricing =
                config.pricing();

        int centerX = width / 2;

        int labelX = centerX - 140;
        int fieldX = centerX + 20;

        int fieldWidth = 120;

        int startY = 55;
        int rowHeight = 40;

        // ─────────────────────────────
        // Сглаживание цены
        // ─────────────────────────────

        addRenderableWidget(
                new StringWidget(
                        labelX,
                        startY + 3,
                        150,
                        20,
                        Component.translatable(
                                "gui.smart_economy.config.smoothing_factor"
                        ),
                        font
                )
        );

        smoothingFactorField = createField(
                fieldX,
                startY,
                fieldWidth,
                pricing.smoothingFactor()
        );

        // ─────────────────────────────
        // Максимальное отклонение цены
        // ─────────────────────────────

        addRenderableWidget(
                new StringWidget(
                        labelX,
                        startY + rowHeight + 3,
                        150,
                        20,
                        Component.translatable(
                                "gui.smart_economy.config.max_price_deviation"
                        ),
                        font
                )
        );

        maxPriceDeviationField = createField(
                fieldX,
                startY + rowHeight,
                fieldWidth,
                pricing.maxPriceDeviation()
        );

        // ─────────────────────────────
        // Кнопки
        // ─────────────────────────────

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

        field.setValue(
                Double.toString(value)
        );

        field.setMaxLength(16);

        addRenderableWidget(field);

        return field;
    }

    private void resetFields() {
        PricingConfig defaults =
                PricingConfig.defaults();

        smoothingFactorField.setValue(
                Double.toString(
                        defaults.smoothingFactor()
                )
        );

        maxPriceDeviationField.setValue(
                Double.toString(
                        defaults.maxPriceDeviation()
                )
        );
    }

    private void saveAndClose() {
        try {
            double smoothingFactor =
                    Double.parseDouble(
                            smoothingFactorField.getValue()
                    );

            double maxPriceDeviation =
                    Double.parseDouble(
                            maxPriceDeviationField.getValue()
                    );

            PricingConfig pricingConfig =
                    new PricingConfig(
                            smoothingFactor,
                            maxPriceDeviation
                    ).validate();

            SmartEconomyConfig current =
                    SmartEconomyConfigManager.get();

            SmartEconomyConfig updated =
                    new SmartEconomyConfig(
                            current.market(),
                            pricingConfig,
                            current.inflation(),
                            current.trading()
                    ).validate();

            SmartEconomyConfigManager.set(updated);
            SmartEconomyConfigManager.save();

            minecraft.gui.setScreen(parent);

        } catch (NumberFormatException ignored) {
            // Некорректное число — ничего не сохраняем.
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