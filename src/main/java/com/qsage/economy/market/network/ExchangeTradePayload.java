package com.qsage.economy.market.network;

import com.qsage.SmartEconomy;
import com.qsage.economy.market.model.TradeSide;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ExchangeTradePayload(
        Identifier itemId,
        TradeSide side,
        long quantity
) implements CustomPacketPayload {

    public static final Type<ExchangeTradePayload> TYPE =
            new Type<>(
                    SmartEconomy.id("exchange_trade")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ExchangeTradePayload
            > CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ExchangeTradePayload::itemId,

            ByteBufCodecs.VAR_INT.map(
                    value -> {
                        TradeSide[] values = TradeSide.values();

                        if (value < 0 || value >= values.length) {
                            throw new IllegalArgumentException(
                                    "Invalid trade side: " + value
                            );
                        }

                        return values[value];
                    },
                    TradeSide::ordinal
            ),
            ExchangeTradePayload::side,

            ByteBufCodecs.VAR_LONG,
            ExchangeTradePayload::quantity,

            ExchangeTradePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}