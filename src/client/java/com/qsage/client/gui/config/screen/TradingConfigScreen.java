package com.qsage.client.gui.config.screen;

import com.qsage.config.SmartEconomyConfig;
import com.qsage.config.SmartEconomyConfigManager;
import com.qsage.config.TradingConfig;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TradingConfigScreen extends Screen {

    private final Screen parent;

    private EditBox maxTransactionQuantityField;
    private EditBox maxTransactionValueField;
    private EditBox cooldownTicksField;

    public TradingConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.smart_economy.config.trading"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        SmartEconomyConfig config =
                SmartEconomyConfigManager.get();

        TradingConfig trading =
                config.trading();

        int centerX = width / 2;

        int labelX = centerX - 140;
        int fieldX = centerX + 20;

        int startY = 55;
        int rowHeight = 35;

        addField(
                labelX,
                fieldX,
                startY,
                "gui.smart_economy.config.max_transaction_quantity",
                Long.toString(
                        trading.maxTransactionQuantity()
                )
        );

        maxTransactionQuantityField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight,
                "gui.smart_economy.config.max_transaction_value",
                Long.toString(
                        trading.maxTransactionValue()
                )
        );

        maxTransactionValueField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight * 2,
                "gui.smart_economy.config.cooldown_ticks",
                Long.toString(
                        trading.cooldownTicks()
                )
        );

        cooldownTicksField = lastField;

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

    private EditBox lastField;

    private void addField(
            int labelX,
            int fieldX,
            int y,
            String translationKey,
            String value
    ) {
        addRenderableWidget(
                new StringWidget(
                        labelX,
                        y + 3,
                        150,
                        20,
                        Component.translatable(translationKey),
                        font
                )
        );

        EditBox field = new EditBox(
                font,
                fieldX,
                y,
                120,
                20,
                Component.empty()
        );

        field.setValue(value);
        field.setMaxLength(16);

        addRenderableWidget(field);

        lastField = field;
    }

    private void resetFields() {
        TradingConfig defaults =
                TradingConfig.defaults();

        maxTransactionQuantityField.setValue(
                Long.toString(
                        defaults.maxTransactionQuantity()
                )
        );

        maxTransactionValueField.setValue(
                Long.toString(
                        defaults.maxTransactionValue()
                )
        );

        cooldownTicksField.setValue(
                Long.toString(
                        defaults.cooldownTicks()
                )
        );
    }

    private void saveAndClose() {
        try {
            long maxTransactionQuantity =
                    Long.parseLong(
                            maxTransactionQuantityField.getValue()
                    );

            long maxTransactionValue =
                    Long.parseLong(
                            maxTransactionValueField.getValue()
                    );

            long cooldownTicks =
                    Long.parseLong(
                            cooldownTicksField.getValue()
                    );

            TradingConfig tradingConfig =
                    new TradingConfig(
                            maxTransactionQuantity,
                            maxTransactionValue,
                            cooldownTicks
                    ).validate();

            SmartEconomyConfig current =
                    SmartEconomyConfigManager.get();

            SmartEconomyConfig updated =
                    new SmartEconomyConfig(
                            current.market(),
                            current.pricing(),
                            current.inflation(),
                            tradingConfig
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