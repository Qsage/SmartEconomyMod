package com.qsage.client.gui.screen;

import com.qsage.client.economy.ClientEconomy;
import com.qsage.client.economy.MoneyFormatter;
import com.qsage.client.gui.PlayerHeadRenderer;
import com.qsage.client.gui.component.toasts.EconomyNotifications;
import com.qsage.client.network.ClientBankNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public final class BankTransferPanel {

    private final EconomyScreen screen;

    private BankPlayer recipient;

    private EditBox amountInput;
    private Button transferButton;
    private Button backButton;

    private boolean visible;

    public BankTransferPanel(EconomyScreen screen) {
        this.screen = screen;
    }

    public void init() {
        var font = Minecraft.getInstance().font;

        int x = screen.guiLeft();
        int y = screen.guiTop();

        amountInput = new EditBox(
                font,
                x + 35,
                y + 110,
                125,
                18,
                Component.translatable(
                        "gui.smart_economy.bank.transfer.amount"
                )
        );

        amountInput.setMaxLength(19);

        amountInput.setResponder(value -> {
            if (!value.isEmpty() && !value.matches("[0-9]+")) {
                String filtered = value.replaceAll("[^0-9]", "");

                if (!filtered.equals(value)) {
                    amountInput.setValue(filtered);
                    return;
                }
            }

            updateButton();
        });

        amountInput.setResponder(value -> updateButton());

        transferButton = Button.builder(
                Component.translatable(
                        "gui.smart_economy.bank.transfer.send"
                ),
                button -> sendTransfer()
        ).bounds(x + 170, y + 110, 95, 20).build();

        backButton = Button.builder(
                Component.translatable(
                        "gui.smart_economy.bank.transfer.back"
                ),
                button -> screen.closeBankTransfer()
        ).bounds(x + 35, y + 145, 90, 20).build();

        screen.addGuiWidget(amountInput, 35, 110);
        screen.addGuiWidget(transferButton, 170, 110);
        screen.addGuiWidget(backButton, 35, 145);

        setVisible(false);
    }

    public void open(BankPlayer player) {
        recipient = player;

        amountInput.setValue("");
        setVisible(true);
        updateButton();
    }

    public void close() {
        recipient = null;
        setVisible(false);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;

        if (amountInput != null) {
            amountInput.visible = visible;
            amountInput.active = visible;
            if (!visible) {
                amountInput.setFocused(false);
            }
        }

        if (transferButton != null) {
            transferButton.visible = visible;
        }

        if (backButton != null) {
            backButton.visible = visible;
            backButton.active = visible;
        }

        updateButton();
    }

    private long parseAmount() {
        if (amountInput == null) {
            return 0;
        }

        try {
            return Long.parseLong(amountInput.getValue());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void updateButton() {
        if (transferButton == null) {
            return;
        }

        long amount = parseAmount();

        transferButton.active =
                visible
                        && recipient != null
                        && amount > 0
                        && amount <= ClientEconomy.getAvailable();
    }

    private void sendTransfer() {
        if (!visible || recipient == null) {
            return;
        }

        long amount = parseAmount();

        if (amount <= 0 || amount > ClientEconomy.getAvailable()) {
            updateButton();
            return;
        }

        if (ClientBankNetworking.transfer(recipient.uuid(), amount)) {
            EconomyNotifications.info(
                    Component.literal("Банк"),
                    Component.literal("Запрос перевода отправлен")
            );

            screen.closeBankTransfer();
        }
    }

    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        if (!visible || recipient == null) {
            return;
        }

        var font = Minecraft.getInstance().font;

        int x = screen.guiLeft();
        int y = screen.guiTop();

        PlayerHeadRenderer.render(
                graphics,
                recipient.uuid(),
                x + 35,
                y + 65,
                24
        );

        graphics.text(
                font,
                Component.translatable(
                        "gui.smart_economy.bank.transfer.recipient"
                ),
                x + 65,
                y + 64,
                0xFF777777,
                false
        );

        graphics.text(
                font,
                recipient.name(),
                x + 65,
                y + 78,
                0xFFFFFFFF,
                false
        );

        graphics.text(
                font,
                Component.literal(
                        MoneyFormatter.format(ClientEconomy.getAvailable())
                ),
                x + 170,
                y + 96,
                0xFFFFD700,
                false
        );

        updateButton();
    }
}