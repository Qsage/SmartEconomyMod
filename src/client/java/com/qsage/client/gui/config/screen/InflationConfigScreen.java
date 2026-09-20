package com.qsage.client.gui.config.screen;

import com.qsage.config.InflationConfig;
import com.qsage.config.SmartEconomyConfig;
import com.qsage.config.SmartEconomyConfigManager;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InflationConfigScreen extends Screen {

    private final Screen parent;

    private EditBox targetInflationField;
    private EditBox kpField;
    private EditBox kiField;
    private EditBox kdField;
    private EditBox maxCorrectionField;
    private EditBox evaluationPeriodField;

    public InflationConfigScreen(Screen parent) {
        super(Component.translatable(
                "gui.smart_economy.config.inflation"
        ));

        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();

        SmartEconomyConfig config =
                SmartEconomyConfigManager.get();

        InflationConfig inflation =
                config.inflation();

        int centerX = width / 2;

        int labelX = centerX - 140;
        int fieldX = centerX + 20;

        int fieldWidth = 120;

        int startY = 35;
        int rowHeight = 28;

        addField(
                labelX,
                fieldX,
                startY,
                "gui.smart_economy.config.target_inflation",
                Double.toString(inflation.targetInflation())
        );

        targetInflationField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight,
                "gui.smart_economy.config.kp",
                Double.toString(inflation.kp())
        );

        kpField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight * 2,
                "gui.smart_economy.config.ki",
                Double.toString(inflation.ki())
        );

        kiField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight * 3,
                "gui.smart_economy.config.kd",
                Double.toString(inflation.kd())
        );

        kdField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight * 4,
                "gui.smart_economy.config.max_correction",
                Double.toString(inflation.maxCorrection())
        );

        maxCorrectionField = lastField;

        addField(
                labelX,
                fieldX,
                startY + rowHeight * 5,
                "gui.smart_economy.config.evaluation_period",
                Long.toString(inflation.evaluationPeriod())
        );

        evaluationPeriodField = lastField;

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

    /*
     * Вспомогательное поле.
     */
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
        InflationConfig defaults =
                InflationConfig.defaults();

        targetInflationField.setValue(
                Double.toString(defaults.targetInflation())
        );

        kpField.setValue(
                Double.toString(defaults.kp())
        );

        kiField.setValue(
                Double.toString(defaults.ki())
        );

        kdField.setValue(
                Double.toString(defaults.kd())
        );

        maxCorrectionField.setValue(
                Double.toString(defaults.maxCorrection())
        );

        evaluationPeriodField.setValue(
                Long.toString(defaults.evaluationPeriod())
        );
    }

    private void saveAndClose() {
        try {
            double targetInflation =
                    Double.parseDouble(
                            targetInflationField.getValue()
                    );

            double kp =
                    Double.parseDouble(
                            kpField.getValue()
                    );

            double ki =
                    Double.parseDouble(
                            kiField.getValue()
                    );

            double kd =
                    Double.parseDouble(
                            kdField.getValue()
                    );

            double maxCorrection =
                    Double.parseDouble(
                            maxCorrectionField.getValue()
                    );

            long evaluationPeriod =
                    Long.parseLong(
                            evaluationPeriodField.getValue()
                    );

            InflationConfig inflationConfig =
                    new InflationConfig(
                            targetInflation,
                            kp,
                            ki,
                            kd,
                            maxCorrection,
                            evaluationPeriod
                    ).validate();

            SmartEconomyConfig current =
                    SmartEconomyConfigManager.get();

            SmartEconomyConfig updated =
                    new SmartEconomyConfig(
                            current.market(),
                            current.pricing(),
                            inflationConfig,
                            current.trading()
                    ).validate();

            SmartEconomyConfigManager.set(updated);
            SmartEconomyConfigManager.save();

            minecraft.gui.setScreen(parent);

        } catch (NumberFormatException ignored) {
            // Некорректное значение — ничего не сохраняем.
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