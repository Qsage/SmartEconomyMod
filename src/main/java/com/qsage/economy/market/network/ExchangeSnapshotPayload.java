package com.qsage.economy.market.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record ExchangeSnapshotPayload(
        List<ExchangeQuotePayload> quotes
) implements CustomPacketPayload {

    public static final Type<ExchangeSnapshotPayload> TYPE =
            new Type<>(
                    SmartEconomy.id("exchange_snapshot")
            );

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            ExchangeSnapshotPayload
            > CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(
                    ArrayList::new,
                    ExchangeQuotePayload.CODEC
            ),
            ExchangeSnapshotPayload::quotes,

            ExchangeSnapshotPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}