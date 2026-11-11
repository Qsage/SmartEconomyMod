package com.qsage.client.gui.screen;

import com.qsage.SmartEconomy;
import com.qsage.client.economy.ClientTransactionHistory;
import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.component.BalanceWidget;
import com.qsage.client.gui.component.GuiComponent;
import com.qsage.client.gui.component.TabButton;
import com.qsage.client.network.ClientBankNetworking;
import com.qsage.client.player.ClientBankFavorites;
import com.qsage.client.player.ClientKnownPlayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;

public class EconomyScreen extends Screen {
    public static final int GUI_WIDTH = 320;
    public static final int GUI_HEIGHT = 180;

    private enum Section { EXCHANGE, AUCTION, BANK, QUESTS }

    private final List<GuiComponent> components = new ArrayList<>();
    private final Map<Section, TabButton> navigationTabs = new EnumMap<>(Section.class);
    private ExchangePanel exchangePanel;
    private BankPanel bankPanel;
    private Section activeSection = Section.EXCHANGE;

    private BankTransferPanel bankTransferPanel;

    private boolean transactionHistoryOpen = false;

    public EconomyScreen() {
        super(Component.translatable("gui.smart_economy.market.title"));
    }

    @Override
    protected void init() {
        super.init();

        components.clear();
        navigationTabs.clear();

        addGuiComponent(new BalanceWidget(), 220, -18);

        exchangePanel = new ExchangePanel(this);
        exchangePanel.init();

        bankPanel = new BankPanel(this);

        bankPanel.setOnFavoriteToggle(
                ClientBankNetworking::toggleFavorite
        );

        bankTransferPanel = new BankTransferPanel(this);
        bankTransferPanel.init();

        bankPanel.setOnRecipientSelected(this::openBankTransfer);

        createNavigation();

        setActiveSection(activeSection);

        if (activeSection == Section.BANK) {
            ClientBankNetworking.requestPlayers();
        }
    }

    private void createNavigation() {
        createNavigationTab(Section.EXCHANGE, 0, Items.EMERALD, "gui.smart_economy.market.title");
        createNavigationTab(Section.AUCTION, 1, Items.DIAMOND, "gui.smart_economy.auction.title");
        createNavigationTab(Section.BANK, 2, Items.GOLD_INGOT, "gui.smart_economy.bank.title");
        createNavigationTab(Section.QUESTS, 3, Items.BOOK, "gui.smart_economy.quests.title");
    }

    private void createNavigationTab(Section section, int index, Item icon, String tooltipKey) {
        TabButton button = new TabButton(
                0, 0,
                GuiStyle.CATEGORY_TAB_LEFT,
                GuiStyle.CATEGORY_TAB_LEFT_HOVER,
                GuiStyle.CATEGORY_TAB_LEFT_ACTIVE,
                Component.translatable(tooltipKey),
                icon.getDefaultInstance(),
                () -> setActiveSection(section)
        );
        addGuiWidget(button, -23, 21 + index * 29);
        navigationTabs.put(section, button);
    }


    private void setActiveSection(Section section) {

        boolean openingBank =
                section == Section.BANK
                        && activeSection != Section.BANK;

        activeSection = section;

        if (exchangePanel != null) {
            exchangePanel.setVisible(
                    section == Section.EXCHANGE
            );
        }

        if (bankPanel != null) {
            bankPanel.setVisible(
                    section == Section.BANK && !transactionHistoryOpen
            );
        }

        if (bankTransferPanel != null) {
            bankTransferPanel.close();
        }

        if (section == Section.BANK) {
            refreshBank();
            if (openingBank) {
                ClientBankNetworking.requestPlayers();
            }
        }

        if (section != Section.BANK && bankPanel != null) {
            bankPanel.setView(BankPanel.BankView.PLAYERS);
        }

        if (section != Section.BANK) {
            transactionHistoryOpen = false;
        }

        navigationTabs.forEach((key, button) ->
                button.setSelected(key == section)
        );
    }

    private void openBankTransfer(BankPlayer player) {
        if (activeSection != Section.BANK) {
            return;
        }

        bankPanel.setVisible(false);
        bankTransferPanel.open(player);
    }

    public void closeBankTransfer() {
        bankTransferPanel.close();

        if (activeSection == Section.BANK) {
            bankPanel.setVisible(true);
        }
    }

    public void refreshExchange() {
        if (exchangePanel != null) exchangePanel.refresh();
    }

    public void refreshBank() {

        SmartEconomy.LOGGER.info(
                "BANK REFRESH: known={}, panel={}",
                ClientKnownPlayers.getAll().size(),
                bankPanel != null
        );

        if (bankPanel == null) {
            return;
        }

        if (Minecraft.getInstance().player == null) {
            return;
        }

        UUID self = Minecraft.getInstance().player.getUUID();

        List<BankPlayer> players = ClientKnownPlayers.getAll()
                .stream()
                .filter(player -> !player.uuid().equals(self))
                .map(player -> new BankPlayer(
                        player.uuid(),
                        player.name(),
                        player.online(),
                        ClientBankFavorites.isFavorite(player.uuid())
                ))
                .toList();

        SmartEconomy.LOGGER.info(
                "BANK: refreshing {} players",
                ClientKnownPlayers.getAll().size()
        );

        bankPanel.setPlayers(players);
    }

    public void refreshBankPlayers(List<BankPlayer> players) {
        if (bankPanel != null) {
            bankPanel.setPlayers(players);
        }
    }

    protected int guiLeft() { return (width - GUI_WIDTH) / 2; }
    protected int guiTop() { return (height - GUI_HEIGHT) / 2; }

    protected <T extends GuiComponent> T addGuiComponent(T component, int x, int y) {
        component.setPosition(guiLeft() + x, guiTop() + y);
        components.add(component);
        return component;
    }

    protected <T extends AbstractWidget> T addGuiWidget(T widget, int x, int y) {
        widget.setX(guiLeft() + x);
        widget.setY(guiTop() + y);
        return addRenderableWidget(widget);
    }

    protected void clearGuiComponents(List<? extends GuiComponent> target) {
        components.removeAll(target);
        target.clear();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        GuiAtlas.draw(
                graphics,
                GuiStyle.BOOK,
                guiLeft(),
                guiTop()
        );

        if (exchangePanel != null) {
            exchangePanel.render(graphics, mouseX, mouseY, delta);
        }

        for (GuiComponent component : components) {
            component.render(graphics, mouseX, mouseY, delta);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);

        if (bankPanel != null) {
            bankPanel.render(graphics, mouseX, mouseY, delta);
        }

        if (bankTransferPanel != null) {
            bankTransferPanel.render(graphics, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick))
            return true;

        if (bankPanel != null
                && bankPanel.mouseClicked(
                event.x(),
                event.y(),
                event.button()
        )) {
            return true;
        }

        if (event.button() != 0)
            return false;

        for (int i = components.size() - 1; i >= 0; i--) {
            if (components.get(i).mouseClicked(event.x(), event.y(), event.button())) return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (bankPanel != null
                && bankPanel.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        )) {
            return true;
        }

        if (exchangePanel != null
                && exchangePanel.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        )) {
            return true;
        }

        return super.mouseScrolled(
                mouseX,
                mouseY,
                scrollX,
                scrollY
        );
    }

    public void openTransactionHistory() {
        if (activeSection != Section.BANK) {
            setActiveSection(Section.BANK);
        }

        if (bankTransferPanel != null) {
            bankTransferPanel.close();
        }

        transactionHistoryOpen = true;

        if (bankPanel != null) {
            bankPanel.setVisible(false);
        }

        ClientTransactionHistory.clear();
        ClientTransactionHistory.requestPage(0);
    }

    public void closeTransactionHistory() {
        transactionHistoryOpen = false;

        if (activeSection == Section.BANK && bankPanel != null) {
            bankPanel.setVisible(true);
        }
    }

    public void refreshTransactionHistory() {
        if (bankPanel != null) {
            bankPanel.refreshHistory();
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return true; }
}
