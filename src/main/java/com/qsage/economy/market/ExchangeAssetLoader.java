package com.qsage.economy.market;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.qsage.economy.market.model.ExchangeAsset;
import com.qsage.economy.market.model.ExchangeCategory;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ExchangeAssetLoader {

    private static final Gson GSON = new GsonBuilder().create();

    private ExchangeAssetLoader() {
    }

    public static List<ExchangeAsset> load(
            ResourceManager resourceManager
    ) {
        Identifier resourceId = Identifier.fromNamespaceAndPath(
                "smart-economy",
                "exchange/assets.json"
        );


        Resource resource = resourceManager
                .getResource(resourceId)
                .orElseThrow(() -> new IllegalStateException(
                        "Exchange assets file not found: " + resourceId
                ));

        try (Reader reader = new InputStreamReader(
                resource.open(),
                StandardCharsets.UTF_8
        )) {

            ExchangeAssetsJson json =
                    GSON.fromJson(reader, ExchangeAssetsJson.class);

            if (json == null || json.assets == null) {
                throw new IllegalStateException(
                        "Exchange assets JSON is empty"
                );
            }

            List<ExchangeAsset> result =
                    new ArrayList<>(json.assets.size());

            for (AssetJson asset : json.assets) {
                result.add(parse(asset));
            }

            return result;

        } catch (JsonParseException e) {
            throw new IllegalStateException(
                    "Failed to parse exchange assets",
                    e
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to load exchange assets",
                    e
            );
        }
    }

    private static ExchangeAsset parse(AssetJson json) {
        if (json.item == null || json.item.isBlank()) {
            throw new IllegalArgumentException(
                    "Exchange asset item is missing"
            );
        }

        if (json.base_price <= 0) {
            throw new IllegalArgumentException(
                    "Base price must be positive: " + json.item
            );
        }

        if (json.value <= 0.0) {
            throw new IllegalArgumentException(
                    "Value must be positive: " + json.item
            );
        }

        Identifier itemId =
                Identifier.parse(json.item);

        long valuePpm = Math.round(
                json.value * ExchangeAsset.VALUE_SCALE
        );

        return new ExchangeAsset(
                itemId,
                json.base_price,
                valuePpm,
                json.expected_volume,
                ExchangeCategory.valueOf(json.category)
        );
    }

    private static final class ExchangeAssetsJson {
        List<AssetJson> assets;
    }

    private static final class AssetJson {
        String item;
        long base_price;
        double value;
        long expected_volume;
        String category;
    }
}