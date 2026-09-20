package com.qsage.economy.market;

import com.qsage.config.SmartEconomyConfigManager;
import com.qsage.config.TradingConfig;
import com.qsage.economy.EconomyService;
import com.qsage.economy.market.model.*;
import com.qsage.economy.market.network.ExchangeQuotePayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import com.qsage.economy.market.pricing.*;
import com.qsage.economy.money.MoneySink;
import com.qsage.economy.money.MoneySource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.Identifier;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeService {

    private final ExchangeRepository repository;
    private final EconomyService economyService;

    private final MarketPressureCalculator pressureCalculator;
    private final PriceSmoother priceSmoother;

    private final Map<Identifier, ExchangeAsset> assets =
            new ConcurrentHashMap<>();

    private final Map<UUID, Long> lastTradeTicks =
            new ConcurrentHashMap<>();

    public ExchangeService(
            ExchangeRepository repository,
            EconomyService economyService
    ) {
        this.repository = repository;
        this.economyService = economyService;

        this.pressureCalculator =
                new DefaultMarketPressureCalculator();

        this.priceSmoother =
                new LinearPriceSmoother(
                        SmartEconomyConfigManager.get()
                                .pricing()
                                .smoothingFactor()
                );
    }

    private ExchangeQuote recalculateQuote(
            ExchangeAsset asset,
            ExchangeQuote quote
    ) {
        Instant now = Instant.now();

        Instant from = now.minus(Duration.ofMinutes(15));

        TradeVolume volume =
                repository.getVolume(
                        asset.itemId(),
                        from,
                        now
                );

        long expectedVolume = Math.max(
                100L,
                asset.basePrice()
        );

        double pressure =
                pressureCalculator.calculatePressure(
                        volume.buyVolume(),
                        volume.sellVolume(),
                        expectedVolume
                );

        double maxPriceDeviation =
                SmartEconomyConfigManager.get()
                        .pricing()
                        .maxPriceDeviation();

        long fundamentalPrice =
                asset.fundamentalPrice();

        double targetMultiplier =
                1.0 + pressure * maxPriceDeviation;

        double minMultiplier =
                1.0 - maxPriceDeviation;

        double maxMultiplier =
                1.0 + maxPriceDeviation;

        targetMultiplier = Math.max(
                minMultiplier,
                Math.min(maxMultiplier, targetMultiplier)
        );

        long targetPrice =
                Math.max(
                        1L,
                        Math.round(
                                fundamentalPrice * targetMultiplier
                        )
                );

        long currentPrice = quote.buyPrice();

        long newPrice =
                priceSmoother.smooth(
                        currentPrice,
                        targetPrice
                );

        return new ExchangeQuote(
                asset.itemId(),
                newPrice,
                newPrice,
                quote.availableQuantity(),
                now.toEpochMilli()
        );
    }

    /*
     * ============================================================
     * Assets
     * ============================================================
     */

    public void loadAssets(
            net.minecraft.server.packs.resources.ResourceManager resourceManager
    ) {
        List<ExchangeAsset> loaded =
                ExchangeAssetLoader.load(resourceManager);

        assets.clear();

        for (ExchangeAsset asset : loaded) {
            registerAsset(asset);
        }

        initializeQuotes();
    }

    public void registerAsset(
            ExchangeAsset asset
    ) {
        if (assets.putIfAbsent(
                asset.itemId(),
                asset
        ) != null) {
            throw new IllegalArgumentException(
                    "Exchange asset already registered: "
                            + asset.itemId()
            );
        }
    }

    public ExchangeAsset getAsset(
            Identifier itemId
    ) {
        return assets.get(itemId);
    }

    public Map<Identifier, ExchangeAsset> getAssets() {
        return Map.copyOf(assets);
    }

    /*
     * ============================================================
     * Initial quotes
     * ============================================================
     */

    public void initializeQuotes() {

        for (ExchangeAsset asset : assets.values()) {

            long fundamentalPrice =
                    PriceCalculator.calculateFundamentalPrice(
                            asset
                    );

            ExchangeQuote existing =
                    repository.getQuote(
                            asset.itemId()
                    );

            if (existing != null) {
                continue;
            }

            ExchangeQuote quote =
                    new ExchangeQuote(
                            asset.itemId(),
                            fundamentalPrice,
                            fundamentalPrice,
                            0,
                            System.currentTimeMillis()
                    );

            repository.saveQuote(quote);
        }
    }

    /*
     * ============================================================
     * Quotes
     * ============================================================
     */

    public ExchangeQuote getQuote(
            Identifier itemId
    ) {
        return repository.getQuote(itemId);
    }

    public ExchangeSnapshotPayload createSnapshot() {

        List<ExchangeQuotePayload> quotes =
                new ArrayList<>();

        for (ExchangeQuote quote : repository.getQuotes()) {

            ExchangeAsset asset =
                    assets.get(quote.itemId());

            if (asset == null) {
                continue;
            }

            quotes.add(
                    new ExchangeQuotePayload(
                            quote.itemId(),
                            quote.buyPrice(),
                            quote.sellPrice(),
                            quote.availableQuantity(),
                            asset.category()
                    )
            );
        }

        return new ExchangeSnapshotPayload(quotes);
    }

    /*
     * ============================================================
     * BUY / SELL
     * ============================================================
     */

    public void trade(
            MinecraftServer server,
            ServerPlayer player,
            Identifier itemId,
            TradeSide side,
            long quantity
    ) {
        TradingConfig trading =
                SmartEconomyConfigManager.get().trading();

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be positive"
            );
        }

        if (quantity > trading.maxTransactionQuantity()) {
            throw new IllegalArgumentException(
                    "Transaction quantity exceeds the configured limit"
            );
        }

        long currentTick = server.getTickCount();

        Long lastTradeTick =
                lastTradeTicks.get(player.getUUID());

        if (lastTradeTick != null &&
                currentTick - lastTradeTick < trading.cooldownTicks()) {
            throw new IllegalArgumentException(
                    "Trading is on cooldown"
            );
        }

        ExchangeAsset asset = assets.get(itemId);

        if (asset == null) {
            throw new IllegalArgumentException(
                    "Item is not traded on exchange: " + itemId
            );
        }

        ExchangeQuote quote = repository.getQuote(itemId);

        if (quote == null) {
            throw new IllegalArgumentException(
                    "Exchange quote not found: " + itemId
            );
        }

        Item item = BuiltInRegistries.ITEM
                .getOptional(itemId)
                .orElse(null);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Unknown item: " + itemId
            );
        }

        switch (side) {
            case BUY -> buy(
                    player,
                    asset,
                    quote,
                    item,
                    quantity
            );

            case SELL -> sell(
                    player,
                    asset,
                    quote,
                    item,
                    quantity
            );
        }
    }

    private void buy(
            ServerPlayer player,
            ExchangeAsset asset,
            ExchangeQuote quote,
            Item item,
            long quantity
    ) {
        if (quote.availableQuantity() < quantity) {
            throw new IllegalArgumentException(
                    "Not enough items on exchange"
            );
        }

        long totalPrice;

        try {
            totalPrice = Math.multiplyExact(
                    quote.buyPrice(),
                    quantity
            );
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Trade value is too large"
            );
        }

        TradingConfig trading =
                SmartEconomyConfigManager.get().trading();

        if (totalPrice > trading.maxTransactionValue()) {
            throw new IllegalArgumentException(
                    "Transaction value exceeds the configured limit"
            );
        }

        economyService.withdraw(
                player.getUUID(),
                totalPrice,
                MoneySink.EXCHANGE_PURCHASE,
                "Exchange purchase: " + asset.itemId()
        );

        addItems(
                player,
                item,
                quantity
        );

        long newAvailableQuantity =
                quote.availableQuantity() - quantity;

        if (newAvailableQuantity < 0) {
            throw new IllegalStateException(
                    "Exchange stock became negative"
            );
        }

        repository.saveTrade(
                new ExchangeTrade(
                        UUID.randomUUID(),
                        player.getUUID(),
                        asset.itemId(),
                        TradeSide.BUY,
                        quantity,
                        quote.buyPrice(),
                        totalPrice,
                        Instant.now()
                )
        );

        ExchangeQuote updatedQuote = new ExchangeQuote(
                quote.itemId(),
                quote.buyPrice(),
                quote.sellPrice(),
                newAvailableQuantity,
                System.currentTimeMillis()
        );

        ExchangeQuote recalculatedQuote =
                recalculateQuote(
                        asset,
                        updatedQuote
                );

        repository.saveQuote(recalculatedQuote);
    }

    private void sell(
            ServerPlayer player,
            ExchangeAsset asset,
            ExchangeQuote quote,
            Item item,
            long quantity
    ) {
        long owned =
                player.getInventory().countItem(item);

        if (owned < quantity) {
            throw new IllegalArgumentException(
                    "Not enough items"
            );
        }

        long totalPrice;

        try {
            totalPrice = Math.multiplyExact(
                    quote.sellPrice(),
                    quantity
            );
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "Trade value is too large"
            );
        }

        TradingConfig trading =
                SmartEconomyConfigManager.get().trading();

        if (totalPrice > trading.maxTransactionValue()) {
            throw new IllegalArgumentException(
                    "Transaction value exceeds the configured limit"
            );
        }

        economyService.deposit(
                player.getUUID(),
                totalPrice,
                MoneySource.EXCHANGE_SALE,
                "Exchange sale: " + asset.itemId()
        );

        long newAvailableQuantity;

        try {
            newAvailableQuantity = Math.addExact(
                    quote.availableQuantity(),
                    quantity
            );
        } catch (ArithmeticException e) {
            throw new IllegalStateException(
                    "Exchange stock is too large"
            );
        }

        ExchangeQuote updatedQuote =
                new ExchangeQuote(
                        quote.itemId(),
                        quote.buyPrice(),
                        quote.sellPrice(),
                        newAvailableQuantity,
                        System.currentTimeMillis()
                );

        repository.saveTrade(
                new ExchangeTrade(
                        UUID.randomUUID(),
                        player.getUUID(),
                        asset.itemId(),
                        TradeSide.SELL,
                        quantity,
                        quote.sellPrice(),
                        totalPrice,
                        Instant.now()
                )
        );

        ExchangeQuote recalculatedQuote =
                recalculateQuote(
                        asset,
                        updatedQuote
                );

        repository.saveQuote(recalculatedQuote);
    }

    /*
     * ============================================================
     * Inventory
     * ============================================================
     */

    private void markTrade(
            MinecraftServer server,
            ServerPlayer player
    ) {
        lastTradeTicks.put(
                player.getUUID(),
                (long) server.getTickCount()
        );
    }

    private boolean canFit(
            ServerPlayer player,
            Item item,
            long quantity
    ) {
        ItemStack prototype =
                item.getDefaultInstance();

        int maxStackSize =
                prototype.getMaxStackSize();

        long remaining = quantity;

        for (ItemStack stack :
                player.getInventory()
                        .getNonEquipmentItems()) {

            if (stack.isEmpty()) {
                remaining -= maxStackSize;
            } else if (!stack.isEmpty() && stack.getItem() == item) {
                remaining -=
                        stack.getMaxStackSize()
                                - stack.getCount();
            }

            if (remaining <= 0) {
                return true;
            }
        }

        return remaining <= 0;
    }

    private void addItems(
            ServerPlayer player,
            Item item,
            long quantity
    ) {
        int maxStackSize =
                item.getDefaultInstance()
                        .getMaxStackSize();

        long remaining = quantity;

        while (remaining > 0) {

            int amount = (int) Math.min(
                    remaining,
                    maxStackSize
            );

            ItemStack stack =
                    new ItemStack(
                            item,
                            amount
                    );

            if (!player.getInventory().add(stack)) {
                throw new IllegalStateException(
                        "Failed to add item to inventory"
                );
            }

            remaining -= amount;
        }
    }

    private boolean removeItems(
            ServerPlayer player,
            Item item,
            long quantity
    ) {
        int remaining = (int) quantity;

        for (ItemStack stack :
                player.getInventory().getNonEquipmentItems()) {

            if (remaining <= 0) {
                break;
            }

            if (stack.isEmpty() || stack.getItem() != item) {
                continue;
            }

            int remove = Math.min(
                    remaining,
                    stack.getCount()
            );

            stack.shrink(remove);
            remaining -= remove;
        }

        return remaining == 0;
    }

    public void refreshQuotes() {
        Instant now = Instant.now();

        for (ExchangeAsset asset : assets.values()) {

            ExchangeQuote oldQuote = repository.getQuote(asset.itemId());

            if (oldQuote == null) {
                long price = asset.fundamentalPrice();

                repository.saveQuote(
                        new ExchangeQuote(
                                asset.itemId(),
                                price,
                                price,
                                0,
                                now.toEpochMilli()
                        )
                );

                continue;
            }

            long price = oldQuote.buyPrice();

            repository.saveQuote(
                    new ExchangeQuote(
                            asset.itemId(),
                            price,
                            price,
                            oldQuote.availableQuantity(),
                            now.toEpochMilli()
                    )
            );
        }
    }
}