package com.qsage.network;

import com.qsage.SmartEconomy;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record BalancePayload(
        long available,
        long locked
) implements CustomPacketPayload {

    public static final Identifier ID =
            SmartEconomy.id("balance");

    public static final Type<BalancePayload> TYPE =
            new Type<>(ID);

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            BalancePayload
            > CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG,
            BalancePayload::available,

            ByteBufCodecs.VAR_LONG,
            BalancePayload::locked,

            BalancePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}