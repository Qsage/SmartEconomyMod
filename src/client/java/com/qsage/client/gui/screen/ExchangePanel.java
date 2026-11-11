package com.qsage.client.gui.screen;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.TextureRegion;
import com.qsage.client.gui.component.GuiComponent;
import com.qsage.client.gui.component.MarketLotWidget;
import com.qsage.client.gui.component.TabButton;
import com.qsage.client.market.ClientExchange;
import com.qsage.client.network.ExchangeNetworking;
import com.qsage.economy.market.model.ExchangeCategory;
import com.qsage.economy.market.network.ExchangeQuotePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static com.qsage.client.gui.GuiStyle.BALANCE_TEXT_MARGIN_RIGHT;

/** The exchange section of EconomyScreen. This is not a Minecraft Screen. */
public final class ExchangePanel {
    private static final int ROW_X = 18;
    private static final int ROW_Y = 21;
    private static final int ROW_HEIGHT = 29;
    private static final int VISIBLE_ROWS = 5;
    private static final int SCROLLBAR_X = 300;
    private static final int SCROLLBAR_Y = ROW_Y;
    private static final int SCROLLBAR_HEIGHT = VISIBLE_ROWS * ROW_HEIGHT;

    private final EconomyScreen screen;
    private final List<GuiComponent> exchangeLots = new ArrayList<>();
    private final Map<SortMode, TabButton> sortButtons = new EnumMap<>(SortMode.class);
    private final Map<ExchangeCategory, TabButton> categoryTabs = new EnumMap<>(ExchangeCategory.class);
    private SortMode sortMode = SortMode.ALPHABETICAL;
    private SortDirection sortDirection = SortDirection.ASCENDING;
    private ExchangeCategory selectedCategory;
    private int scrollOffset;
    private EditBox searchBox;
    private String searchText = "";
    private boolean visible = true;

    private enum SortDirection { ASCENDING, DESCENDING }

    public enum SortMode {
        ALPHABETICAL(GuiStyle.SORT_ICON_ALPHA),
        PRICE(GuiStyle.SORT_ICON_PRICE),
        QUANTITY(GuiStyle.SORT_ICON_QUANTITY);

        private final TextureRegion icon;
        SortMode(TextureRegion icon) { this.icon = icon; }
        public TextureRegion icon() { return icon; }
    }

    public ExchangePanel(EconomyScreen screen) {
        this.screen = screen;
    }

    public void init() {
        ExchangeNetworking.requestRefresh();
        sortButtons.clear();
        categoryTabs.clear();
        exchangeLots.clear();

        searchBox = new EditBox(
                Minecraft.getInstance().font,
                screen.guiLeft() + BALANCE_TEXT_MARGIN_RIGHT,
                screen.guiTop() - 14,
                94, 12, Component.empty()
        );
        searchBox.setBordered(false);
        searchBox.setMaxLength(32);
        searchBox.setHint(Component.translatable("gui.smart_economy.market.search"));
        searchBox.setValue(searchText);
        searchBox.setResponder(text -> {
            searchText = text;
            scrollOffset = 0;
            rebuildMarketLots();
        });
        screen.addGuiWidget(searchBox, BALANCE_TEXT_MARGIN_RIGHT, -14);
        createCategoryTabs();
        createSortButtons();
        setVisible(visible);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        if (searchBox != null) {
            searchBox.visible = visible;
            searchBox.active = visible;
            if (!visible) searchBox.setFocused(false);
        }
        for (TabButton button : categoryTabs.values()) {
            button.visible = visible;
            button.active = visible;
        }
        for (TabButton button : sortButtons.values()) {
            button.visible = visible;
            button.active = visible;
        }
        if (visible) rebuildMarketLots();
        else screen.clearGuiComponents(exchangeLots);
    }

    public void refresh() {
        scrollOffset = Math.min(scrollOffset, getMaxScroll());
        if (visible) rebuildMarketLots();
    }

    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!visible) return;
        GuiAtlas.draw(graphics, GuiStyle.SEARCH, screen.guiLeft(), screen.guiTop() - 18);
        renderScrollBar(graphics);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!visible || scrollY == 0 || !isMouseOverMarket(mouseX, mouseY)) return false;
        int old = scrollOffset;
        scrollOffset = Math.max(0, Math.min(getMaxScroll(),
                scrollOffset - (int) Math.signum(scrollY)));
        if (old != scrollOffset) rebuildMarketLots();
        return true;
    }

    private boolean isMouseOverMarket(double mouseX, double mouseY) {
        int x = screen.guiLeft();
        int y = screen.guiTop();
        return mouseX >= x + ROW_X && mouseX <= x + SCROLLBAR_X + 8
                && mouseY >= y + ROW_Y && mouseY < y + ROW_Y + SCROLLBAR_HEIGHT;
    }

    private List<ExchangeQuotePayload> getFilteredQuotes() {
        String query = searchText.trim().toLowerCase(Locale.ROOT);
        List<ExchangeQuotePayload> quotes = ClientExchange.getAll().values().stream()
                .filter(quote -> selectedCategory == null || quote.category() == selectedCategory)
                .filter(quote -> query.isEmpty() || getLocalizedItemName(quote)
                        .toLowerCase(Locale.ROOT).contains(query))
                .collect(Collectors.toCollection(ArrayList::new));

        Comparator<ExchangeQuotePayload> comparator = switch (sortMode) {
            case ALPHABETICAL -> Comparator.comparing(this::getLocalizedItemName,
                    String.CASE_INSENSITIVE_ORDER);
            case PRICE -> Comparator.comparingLong(ExchangeQuotePayload::buyPrice);
            case QUANTITY -> Comparator.comparingLong(ExchangeQuotePayload::availableQuantity);
        };
        if (sortDirection == SortDirection.DESCENDING) comparator = comparator.reversed();
        quotes.sort(comparator.thenComparing(quote -> quote.itemId().toString()));
        return quotes;
    }

    private String getLocalizedItemName(ExchangeQuotePayload quote) {
        Item item = BuiltInRegistries.ITEM.getOptional(quote.itemId()).orElse(null);
        if (item == null || item == Items.AIR) return quote.itemId().toString();
        return item.getDefaultInstance().getHoverName().getString();
    }

    private int getMaxScroll() {
        return Math.max(0, getFilteredQuotes().size() - VISIBLE_ROWS);
    }

    private float getScrollProgress() {
        int max = getMaxScroll();
        return max == 0 ? 0 : (float) scrollOffset / max;
    }

    private int getScrollThumbHeight() {
        int count = getFilteredQuotes().size();
        if (count <= VISIBLE_ROWS) return SCROLLBAR_HEIGHT;
        return Math.max(8, SCROLLBAR_HEIGHT * VISIBLE_ROWS / count);
    }

    private int getScrollThumbY() {
        return screen.guiTop() + SCROLLBAR_Y + Math.round(
                (SCROLLBAR_HEIGHT - getScrollThumbHeight()) * getScrollProgress());
    }

    private void rebuildMarketLots() {
        screen.clearGuiComponents(exchangeLots);
        if (!visible) return;
        List<ExchangeQuotePayload> quotes = getFilteredQuotes();
        scrollOffset = Math.max(0, Math.min(scrollOffset,
                Math.max(0, quotes.size() - VISIBLE_ROWS)));
        int end = Math.min(scrollOffset + VISIBLE_ROWS, quotes.size());
        for (int index = scrollOffset; index < end; index++) {
            ExchangeQuotePayload quote = quotes.get(index);
            Item item = BuiltInRegistries.ITEM.getOptional(quote.itemId()).orElse(null);
            if (item == null || item == Items.AIR) continue;
            ItemStack stack = item.getDefaultInstance();
            long price = quote.buyPrice();
            long stock = quote.availableQuantity();
            MarketLotWidget.Trend trend = MarketLotWidget.Trend.NEUTRAL;
            MarketLotWidget lot = new MarketLotWidget(
                    stack, stack.getHoverName(), price, stock, trend, GuiStyle.MARKET_LOT,
                    () -> Minecraft.getInstance().gui.setScreen(
                            new MarketTradeScreen(screen, stack, price, trend))
            );
            screen.addGuiComponent(lot, ROW_X, ROW_Y + (index - scrollOffset) * ROW_HEIGHT);
            exchangeLots.add(lot);
        }
    }

    private void createCategoryTabs() {
        // The category icons are items, so no additional atlas icon rendering is needed.
        ExchangeCategory[] categories = ExchangeCategory.values();
        for (int i = 0; i < categories.length; i++) {
            ExchangeCategory category = categories[i];
            TabButton button = new TabButton(
                    0, 0,
                    GuiStyle.CATEGORY_TAB,
                    GuiStyle.CATEGORY_TAB_HOVER,
                    GuiStyle.CATEGORY_TAB_ACTIVE,
                    getCategoryTooltip(category),
                    getCategoryIcon(category),
                    () -> selectCategory(category)
            );
            screen.addGuiWidget(button, 293, ROW_Y + i * ROW_HEIGHT);
            categoryTabs.put(category, button);
        }
        updateCategoryTabs();
    }

    private ItemStack getCategoryIcon(ExchangeCategory category) {
        return switch (category) {
            case BUILDING_BLOCKS -> new ItemStack(Items.BRICKS);
            case MATERIALS -> new ItemStack(Items.IRON_INGOT);
            case TOOLS_AND_WEAPONS -> new ItemStack(Items.DIAMOND_PICKAXE);
            case FOOD -> new ItemStack(Items.GOLDEN_APPLE);
            case REDSTONE -> new ItemStack(Items.REDSTONE);
        };
    }

    private void selectCategory(ExchangeCategory category) {
        selectedCategory = selectedCategory == category ? null : category;
        scrollOffset = 0;
        updateCategoryTabs();
        rebuildMarketLots();
    }

    private void updateCategoryTabs() {
        for (Map.Entry<ExchangeCategory, TabButton> entry : categoryTabs.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == selectedCategory);
        }
    }

    private void createSortButtons() {
        SortMode[] modes = SortMode.values();
        for (int i = 0; i < modes.length; i++) {
            SortMode mode = modes[i];
            TabButton button = new TabButton(
                    0, 0,
                    GuiStyle.SORT_BUTTON,
                    GuiStyle.SORT_BUTTON_HOVER,
                    GuiStyle.SORT_BUTTON_ACTIVE,
                    getSortTooltip(mode),
                    getSortIconForButton(mode),
                    () -> selectSortMode(mode)
            );
            screen.addGuiWidget(button, 238 + i * 14, 9);
            sortButtons.put(mode, button);
        }
        updateSortButtons();
    }

    private void selectSortMode(SortMode mode) {
        if (sortMode == mode) {
            sortDirection = sortDirection == SortDirection.ASCENDING
                    ? SortDirection.DESCENDING : SortDirection.ASCENDING;
        } else {
            sortMode = mode;
            sortDirection = SortDirection.DESCENDING;
        }
        scrollOffset = 0;
        updateSortButtons();
        rebuildMarketLots();
    }

    private void updateSortButtons() {
        for (Map.Entry<SortMode, TabButton> entry : sortButtons.entrySet()) {
            entry.getValue().setSelected(entry.getKey() == sortMode);
            entry.getValue().setIcon(getSortIconForButton(entry.getKey()));
        }
    }

    private TextureRegion getSortIconForButton(SortMode mode) {
        SortDirection direction = mode == sortMode ? sortDirection : SortDirection.DESCENDING;
        TextureRegion base = mode.icon();
        return direction == SortDirection.ASCENDING ? base : base.offsetU(base.width());
    }

    private Component getSortTooltip(SortMode mode) {
        return Component.translatable(switch (mode) {
            case ALPHABETICAL -> "gui.smart_economy.market.sort.alphabetical";
            case PRICE -> "gui.smart_economy.market.sort.price";
            case QUANTITY -> "gui.smart_economy.market.sort.quantity";
        });
    }

    private Component getCategoryTooltip(ExchangeCategory category) {
        return Component.translatable(switch (category) {
            case BUILDING_BLOCKS -> "gui.smart_economy.category.building_blocks";
            case TOOLS_AND_WEAPONS -> "gui.smart_economy.category.tools_and_weapons";
            case MATERIALS -> "gui.smart_economy.category.materials";
            case FOOD -> "gui.smart_economy.category.food";
            case REDSTONE -> "gui.smart_economy.category.redstone";
        });
    }

    private void renderScrollBar(GuiGraphicsExtractor graphics) {
        if (getFilteredQuotes().size() <= VISIBLE_ROWS) return;
        GuiAtlas.draw(graphics, GuiStyle.SCROLLBAR,
                screen.guiLeft() + SCROLLBAR_X, screen.guiTop() + SCROLLBAR_Y);
        GuiAtlas.draw(graphics, GuiStyle.SCROLLBAR_THUMB,
                screen.guiLeft() + SCROLLBAR_X, getScrollThumbY());
    }
}
