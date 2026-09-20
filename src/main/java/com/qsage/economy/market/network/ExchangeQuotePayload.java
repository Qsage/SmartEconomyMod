package com.qsage.economy.market.network;

import com.qsage.SmartEconomy;
import com.qsage.economy.market.model.ExchangeCategory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ExchangeQuotePayload(
        Identifier itemId,
        long buyPrice,
        long sellPrice,
        long availableQuantity,
        ExchangeCategory category
) implements CustomPacketPayload {

    public static final Type<ExchangeQuotePayload> TYPE =
            new Type<>(
                    SmartEconomy.id("exchange_quote")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ExchangeQuotePayload
            > CODEC = StreamCodec.composite(

            Identifier.STREAM_CODEC,
            ExchangeQuotePayload::itemId,

            ByteBufCodecs.VAR_LONG,
            ExchangeQuotePayload::buyPrice,

            ByteBufCodecs.VAR_LONG,
            ExchangeQuotePayload::sellPrice,

            ByteBufCodecs.VAR_LONG,
            ExchangeQuotePayload::availableQuantity,

            ByteBufCodecs.VAR_INT.map(
                    value -> {
                        ExchangeCategory[] values =
                                ExchangeCategory.values();

                        if (value < 0 || value >= values.length) {
                            throw new IllegalArgumentException(
                                    "Invalid exchange category: " + value
                            );
                        }

                        return values[value];
                    },
                    ExchangeCategory::ordinal
            ),
            ExchangeQuotePayload::category,

            ExchangeQuotePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}