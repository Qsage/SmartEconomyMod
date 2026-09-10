package com.qsage.economy.market.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ExchangeQuotePayload(
        Identifier itemId,
        long buyPrice,
        long sellPrice,
        long availableQuantity
) implements CustomPacketPayload {

    public static final Identifier ID =
            Identifier.fromNamespaceAndPath(
                    "smart-economy",
                    "exchange_quote"
            );

    public static final Type<ExchangeQuotePayload> TYPE =
            new Type<>(ID);

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

            ExchangeQuotePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}