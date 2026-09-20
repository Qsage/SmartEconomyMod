package com.qsage.client.gui.screen;

import com.qsage.client.gui.GuiAtlas;
import com.qsage.client.gui.TextureRegion;
import com.qsage.client.gui.component.GuiComponent;
import com.qsage.client.market.ClientExchange;
import com.qsage.client.gui.GuiStyle;
import com.qsage.client.gui.component.BalanceWidget;
import com.qsage.client.gui.component.MarketLotWidget;
import com.qsage.client.gui.component.TabButton;
import com.qsage.client.network.ExchangeNetworking;
import com.qsage.economy.market.model.ExchangeCategory;
import com.qsage.economy.market.network.ExchangeQuotePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static com.qsage.client.gui.GuiStyle.BALANCE_TEXT_MARGIN_RIGHT;

public class TestEconomyScreen extends EconomyScreen {

    private static final int ROW_Y = 21;
    private static final int ROW_HEIGHT = 29;
    private static final int VISIBLE_ROWS = 5;

    private enum SortDirection {
        ASCENDING,
        DESCENDING
    }

    public enum SortMode {

        ALPHABETICAL(
                GuiStyle.SORT_ICON_ALPHA
        ),

        PRICE(
                GuiStyle.SORT_ICON_PRICE
        ),

        QUANTITY(
                GuiStyle.SORT_ICON_QUANTITY
        );

        private final TextureRegion icon;

        SortMode(TextureRegion icon) {
            this.icon = icon;
        }

        public TextureRegion icon() {
            return icon;
        }
    }

    private SortMode sortMode =
            SortMode.ALPHABETICAL;

    private SortDirection sortDirection =
            SortDirection.ASCENDING;

    private TextureRegion getSortIcon(
            SortMode mode,
            SortDirection direction
    ) {
        TextureRegion base = switch (mode) {
            case ALPHABETICAL -> GuiStyle.SORT_ICON_ALPHA;
            case PRICE -> GuiStyle.SORT_ICON_PRICE;
            case QUANTITY -> GuiStyle.SORT_ICON_QUANTITY;
        };

        return switch (direction) {
            case ASCENDING ->
                    base;

            case DESCENDING ->
                    base.offsetU(base.width());
        };
    }

    private TextureRegion getSortIconForButton(
            SortMode mode
    ) {
        SortDirection direction;

        if (mode == sortMode) {
            direction = sortDirection;
        } else {
            direction = SortDirection.DESCENDING;
        }

        return getSortIcon(
                mode,
                direction
        );
    }

    private final Map<SortMode, TabButton> sortButtons =
            new EnumMap<>(SortMode.class);

    private static final int SCROLLBAR_X = 300;
    private static final int SCROLLBAR_Y = ROW_Y;

    private static final int SCROLLBAR_HEIGHT =
            VISIBLE_ROWS * ROW_HEIGHT;

    private ExchangeCategory selectedCategory = null;

    private List<ExchangeQuotePayload> getFilteredQuotes() {

        String query =
                searchText
                        .trim()
                        .toLowerCase(Locale.ROOT);

        List<ExchangeQuotePayload> quotes =
                ClientExchange.getAll()
                        .values()
                        .stream()
                        .filter(quote -> {

                            if (selectedCategory != null
                                    && quote.category() != selectedCategory) {

                                return false;
                            }

                            if (query.isEmpty()) {
                                return true;
                            }

                            return getLocalizedItemName(quote)
                                    .toLowerCase(Locale.ROOT)
                                    .contains(query);
                        })
                        .collect(
                                Collectors.toCollection(ArrayList::new)
                        );

        Comparator<ExchangeQuotePayload> comparator;

        switch (sortMode) {

            case ALPHABETICAL:
                comparator =
                        Comparator.comparing(
                                this::getLocalizedItemName,
                                String.CASE_INSENSITIVE_ORDER
                        );
                break;

            case PRICE:
                comparator =
                        Comparator.comparingLong(
                                ExchangeQuotePayload::buyPrice
                        );
                break;

            case QUANTITY:
                comparator =
                        Comparator.comparingLong(
                                ExchangeQuotePayload::availableQuantity
                        );
                break;

            default:
                comparator =
                        Comparator.comparing(
                                this::getLocalizedItemName,
                                String.CASE_INSENSITIVE_ORDER
                        );
                break;
        }

        if (sortDirection == SortDirection.DESCENDING) {
            comparator = comparator.reversed();
        }

        quotes.sort(comparator);

        return quotes;
    }

    private String getLocalizedItemName(
            ExchangeQuotePayload quote
    ) {
        Item item =
                BuiltInRegistries.ITEM
                        .getOptional(quote.itemId())
                        .orElse(null);

        if (item == null || item == Items.AIR) {
            return quote.itemId().toString();
        }

        return item.getDefaultInstance()
                .getHoverName()
                .getString();
    }


    private int scrollOffset = 0;

    private int getMaxScroll() {
        return Math.max(
                0,
                getFilteredQuotes().size() - VISIBLE_ROWS
        );
    }

    private float getScrollProgress() {

        int maxScroll = getMaxScroll();

        if (maxScroll <= 0) {
            return 0.0f;
        }

        return (float) scrollOffset / maxScroll;
    }

    private int getScrollThumbHeight() {

        int itemCount =
                getFilteredQuotes().size();

        if (itemCount <= VISIBLE_ROWS) {
            return SCROLLBAR_HEIGHT;
        }

        return Math.max(
                8,
                SCROLLBAR_HEIGHT
                        * VISIBLE_ROWS
                        / itemCount
        );
    }

    private int getScrollThumbY() {

        int thumbHeight =
                getScrollThumbHeight();

        int available =
                SCROLLBAR_HEIGHT
                        - thumbHeight;

        return guiTop()
                + SCROLLBAR_Y
                + Math.round(
                available
                        * getScrollProgress()
        );
    }

    private EditBox searchBox;
    private String searchText = "";

    private final List<GuiComponent> exchangeLots =
            new ArrayList<>();

    private final Map<ExchangeCategory, TabButton> categoryTabs =
            new EnumMap<>(ExchangeCategory.class);

    public TestEconomyScreen() {
        super(Component.translatable(
                "gui.smart_economy.market.title"
        ));
    }


    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void refreshExchange() {
        int maxScroll =
                Math.max(
                        0,
                        getFilteredQuotes().size() - VISIBLE_ROWS
                );

        scrollOffset =
                Math.min(
                        scrollOffset,
                        maxScroll
                );

        rebuildMarketLots();
    }


    private void rebuildMarketLots() {

        clearGuiComponents(exchangeLots);

        List<ExchangeQuotePayload> quotes =
                getFilteredQuotes();

        int maxScroll =
                Math.max(
                        0,
                        quotes.size() - VISIBLE_ROWS
                );

        scrollOffset =
                Math.min(
                        scrollOffset,
                        maxScroll
                );

        int start = scrollOffset;

        int end =
                Math.min(
                        start + VISIBLE_ROWS,
                        quotes.size()
                );

        for (int index = start; index < end; index++) {

            ExchangeQuotePayload quote =
                    quotes.get(index);

            Item item =
                    BuiltInRegistries.ITEM
                            .getOptional(quote.itemId())
                            .orElse(null);

            if (item == null || item == Items.AIR) {
                continue;
            }

            ItemStack stack =
                    item.getDefaultInstance();

            Component name =
                    stack.getHoverName();

            long price =
                    quote.buyPrice();

            long stock =
                    quote.availableQuantity();

            MarketLotWidget.Trend trend =
                    MarketLotWidget.Trend.NEUTRAL;

            MarketLotWidget lot =
                    new MarketLotWidget(
                            stack,
                            name,
                            price,
                            stock,
                            trend,
                            GuiStyle.MARKET_LOT,
                            () -> Minecraft.getInstance()
                                    .gui
                                    .setScreen(
                                            new MarketTradeScreen(
                                                    this,
                                                    stack,
                                                    price,
                                                    trend
                                            )
                                    )
                    );

            int visibleRow =
                    index - scrollOffset;

            addGuiComponent(
                    lot,
                    18,
                    ROW_Y + visibleRow * ROW_HEIGHT
            );

            exchangeLots.add(lot);
        }
    }




    @Override
    protected void initEconomyScreen() {
        ExchangeNetworking.requestRefresh();

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

        searchBox = new EditBox(
                this.minecraft.font,
                guiLeft() + BALANCE_TEXT_MARGIN_RIGHT,
                guiTop() - 14,
                94,
                12,
                Component.empty()
        );

        searchBox.setBordered(false);
        searchBox.setMaxLength(32);
        searchBox.setHint(
                Component.translatable(
                        "gui.smart_economy.market.search"
                )
        );

        searchBox.setResponder(text -> {
            searchText = text;
            scrollOffset = 0;
            rebuildMarketLots();
        });

        addRenderableWidget(searchBox);

        /*
         * ========================================================
         * Exchange lots and tabs
         * ========================================================
         */

        createCategoryTabs();
        createSortButtons();
        rebuildMarketLots();
    }

    @Override
    protected void renderEconomyOverlay(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        GuiAtlas.draw(
                graphics,
                GuiStyle.SEARCH,
                guiLeft(),
                guiTop() - 18
        );

        renderScrollBar(graphics);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        if (!isMouseOverMarket(mouseX, mouseY)) {
            return super.mouseScrolled(
                    mouseX,
                    mouseY,
                    scrollX,
                    scrollY
            );
        }

        List<ExchangeQuotePayload> quotes =
                getFilteredQuotes();

        int maxScroll =
                Math.max(
                        0,
                        quotes.size() - VISIBLE_ROWS
                );

        if (maxScroll <= 0) {
            return true;
        }

        if (scrollY < 0) {
            scrollOffset++;
        } else if (scrollY > 0) {
            scrollOffset--;
        }

        scrollOffset =
                Math.max(
                        0,
                        Math.min(
                                scrollOffset,
                                maxScroll
                        )
                );

        rebuildMarketLots();

        return true;
    }

    private boolean isMouseOverMarket(
            double mouseX,
            double mouseY
    ) {
        return mouseX >= guiLeft() + 18
                && mouseX <= guiLeft() + 280
                && mouseY >= guiTop() + ROW_Y
                && mouseY <= guiTop()
                + ROW_Y
                + VISIBLE_ROWS * ROW_HEIGHT;
    }

    private void createExchangeLots() {

        exchangeLots.clear();

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

            long price = quote.buyPrice();
            long stock = quote.availableQuantity();

            MarketLotWidget.Trend trend =
                    MarketLotWidget.Trend.NEUTRAL;

            MarketLotWidget lot = new MarketLotWidget(
                    stack,
                    name,
                    price,
                    stock,
                    trend,
                    GuiStyle.MARKET_LOT,
                    () -> Minecraft.getInstance().gui.setScreen(
                            new MarketTradeScreen(
                                    this,
                                    stack,
                                    price,
                                    trend
                            )
                    )
            );

            addGuiComponent(
                    lot,
                    21,
                    ROW_Y + index * ROW_HEIGHT
            );

            exchangeLots.add(lot);

            index++;
        }
    }

    private void selectSortMode(
            SortMode mode
    ) {
        if (sortMode == mode) {

            sortDirection =
                    sortDirection == SortDirection.ASCENDING
                            ? SortDirection.DESCENDING
                            : SortDirection.ASCENDING;

        } else {

            sortMode = mode;
            sortDirection = SortDirection.DESCENDING;
        }

        scrollOffset = 0;

        updateSortButtons();
        rebuildMarketLots();
    }

    private void updateSortButtons() {

        for (Map.Entry<SortMode, TabButton> entry
                : sortButtons.entrySet()) {

            SortMode mode =
                    entry.getKey();

            TabButton button =
                    entry.getValue();

            button.setSelected(
                    mode == sortMode
            );

            button.setIcon(
                    getSortIconForButton(mode)
            );
        }
    }



    private void createSortButtons() {

        SortMode[] modes = {
                SortMode.ALPHABETICAL,
                SortMode.PRICE,
                SortMode.QUANTITY
        };

        for (int index = 0; index < modes.length; index++) {

            SortMode mode = modes[index];

            TabButton button = new TabButton(
                    guiLeft() + 238 + index * 14,
                    guiTop() + 9,

                    GuiStyle.SORT_BUTTON,
                    GuiStyle.SORT_BUTTON_HOVER,
                    GuiStyle.SORT_BUTTON_ACTIVE,

                    getSortTooltip(mode),

                    getSortIconForButton(mode),

                    () -> selectSortMode(mode)
            );

            button.setSelected(
                    sortMode == mode
            );

            addRenderableWidget(button);

            sortButtons.put(
                    mode,
                    button
            );
        }
    }

    private Component getSortTooltip(
            SortMode mode
    ) {
        return Component.translatable(
                switch (mode) {
                    case ALPHABETICAL ->
                            "gui.smart_economy.market.sort.alphabetical";

                    case PRICE ->
                            "gui.smart_economy.market.sort.price";

                    case QUANTITY ->
                            "gui.smart_economy.market.sort.quantity";
                }
        );
    }

    private ItemStack getCategoryIcon(
            ExchangeCategory category
    ) {
        return switch (category) {

            case BUILDING_BLOCKS ->
                    Items.BRICKS.getDefaultInstance();

            case TOOLS_AND_WEAPONS ->
                    Items.DIAMOND_PICKAXE.getDefaultInstance();

            case MATERIALS ->
                    Items.IRON_INGOT.getDefaultInstance();

            case FOOD ->
                    Items.GOLDEN_APPLE.getDefaultInstance();

            case REDSTONE ->
                    Items.REDSTONE.getDefaultInstance();
        };
    }

    private void createCategoryTabs() {

        ExchangeCategory[] categories =
                ExchangeCategory.values();

        for (int index = 0; index < categories.length; index++) {

            ExchangeCategory category =
                    categories[index];

            TabButton tab = new TabButton(
                    guiLeft() + 293,
                    guiTop() + ROW_Y + index * ROW_HEIGHT,

                    GuiStyle.CATEGORY_TAB,
                    GuiStyle.CATEGORY_TAB,
                    GuiStyle.CATEGORY_TAB_ACTIVE,

                    getCategoryTooltip(category),

                    getCategoryIcon(category),

                    () -> selectCategory(category)
            );

            tab.setSelected(
                    selectedCategory == category
            );

            addRenderableWidget(tab);

            categoryTabs.put(
                    category,
                    tab
            );
        }
    }

    private Component getCategoryTooltip(
            ExchangeCategory category
    ) {
        return Component.translatable(
                switch (category) {
                    case BUILDING_BLOCKS ->
                            "gui.smart_economy.category.building_blocks";

                    case TOOLS_AND_WEAPONS ->
                            "gui.smart_economy.category.tools_and_weapons";

                    case MATERIALS ->
                            "gui.smart_economy.category.materials";

                    case FOOD ->
                            "gui.smart_economy.category.food";

                    case REDSTONE ->
                            "gui.smart_economy.category.redstone";
                }
        );
    }

    private void selectCategory(
            ExchangeCategory category
    ) {
        if (selectedCategory == category) {
            selectedCategory = null;
        } else {
            selectedCategory = category;
        }

        scrollOffset = 0;

        updateCategoryTabs();
        rebuildMarketLots();
    }

    private void updateCategoryTabs() {

        for (Map.Entry<ExchangeCategory, TabButton> entry
                : categoryTabs.entrySet()) {

            entry.getValue().setSelected(
                    entry.getKey() == selectedCategory
            );
        }
    }

    private void renderScrollBar(
            GuiGraphicsExtractor graphics
    ) {
        int itemCount =
                getFilteredQuotes().size();

        if (itemCount <= VISIBLE_ROWS) {
            return;
        }

        GuiAtlas.draw(
                graphics,
                GuiStyle.SCROLLBAR,
                guiLeft() + SCROLLBAR_X,
                guiTop() + SCROLLBAR_Y
        );

        GuiAtlas.draw(
                graphics,
                GuiStyle.SCROLLBAR_THUMB,
                guiLeft() + SCROLLBAR_X,
                getScrollThumbY()
        );
    }
}