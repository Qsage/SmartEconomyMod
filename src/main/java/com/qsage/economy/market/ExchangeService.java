package com.qsage.economy.market;

import com.qsage.economy.market.model.ExchangeAsset;
import com.qsage.economy.market.model.ExchangeQuote;
import com.qsage.economy.market.network.ExchangeQuotePayload;
import com.qsage.economy.market.network.ExchangeSnapshotPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExchangeService {

    private final Map<Identifier, ExchangeAsset> assets =
            new ConcurrentHashMap<>();

    private final Map<Identifier, ExchangeQuote> quotes =
            new ConcurrentHashMap<>();

    public void registerAsset(ExchangeAsset asset) {
        if (assets.putIfAbsent(asset.itemId(), asset) != null) {
            throw new IllegalArgumentException(
                    "Exchange asset already registered: " + asset.itemId()
            );
        }
    }

    public void loadAssets(ResourceManager resourceManager) {
        List<ExchangeAsset> loaded =
                ExchangeAssetLoader.load(resourceManager);

        assets.clear();

        for (ExchangeAsset asset : loaded) {
            registerAsset(asset);
        }

        initializeQuotes();
    }

    public ExchangeSnapshotPayload createSnapshot() {

        List<ExchangeQuotePayload> payloads =
                new ArrayList<>();

        for (ExchangeQuote quote : quotes.values()) {

            payloads.add(
                    new ExchangeQuotePayload(
                            quote.itemId(),
                            quote.buyPrice(),
                            quote.sellPrice(),
                            quote.availableQuantity()
                    )
            );
        }

        return new ExchangeSnapshotPayload(payloads);
    }

    public ExchangeAsset getAsset(Identifier itemId) {
        return assets.get(itemId);
    }

    public ExchangeQuote getQuote(Identifier itemId) {
        return quotes.get(itemId);
    }

    public void updateQuote(ExchangeQuote quote) {
        quotes.put(quote.itemId(), quote);
    }

    public void initializeQuotes() {
        quotes.clear();

        for (ExchangeAsset asset : assets.values()) {

            long price = asset.fundamentalPrice();

            ExchangeQuote quote = new ExchangeQuote(
                    asset.itemId(),
                    price,
                    price,
                    0,
                    System.currentTimeMillis()
            );

            quotes.put(
                    asset.itemId(),
                    quote
            );
        }
    }

    public Map<Identifier, ExchangeAsset> getAssets() {
        return Map.copyOf(assets);
    }
}