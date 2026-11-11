
package com.qsage.economy.player.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record KnownPlayersRequestPayload()
        implements CustomPacketPayload {

    public static final Type<KnownPlayersRequestPayload> TYPE =
            new Type<>(SmartEconomy.id("known_players_request"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            KnownPlayersRequestPayload
            > CODEC = StreamCodec.unit(new KnownPlayersRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
