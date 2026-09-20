package com.qsage.economy.market.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;

public record ExchangeRefreshPayload() implements CustomPacketPayload {

    public static final Type<ExchangeRefreshPayload> TYPE =
            new Type<>(SmartEconomy.id("exchange_refresh"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExchangeRefreshPayload> CODEC =
            StreamCodec.unit(new ExchangeRefreshPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}