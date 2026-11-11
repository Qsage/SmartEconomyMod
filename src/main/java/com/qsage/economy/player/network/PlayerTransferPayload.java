package com.qsage.economy.player.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record PlayerTransferPayload (
        UUID target,
        long amount
)  implements CustomPacketPayload {

    public static final Type<PlayerTransferPayload> TYPE =
            new Type<>(SmartEconomy.id("player_transfer"));

    private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_CODEC =
            new StreamCodec<>() {
                @Override
                public UUID decode(RegistryFriendlyByteBuf buf) {
                    return new UUID(
                            buf.readLong(),
                            buf.readLong()
                    );
                }

                @Override
                public void encode(
                        RegistryFriendlyByteBuf buf,
                        UUID uuid
                ) {
                    buf.writeLong(uuid.getMostSignificantBits());
                    buf.writeLong(uuid.getLeastSignificantBits());
                }
            };

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerTransferPayload> CODEC =
            StreamCodec.composite(
                    UUID_CODEC,
                    PlayerTransferPayload::target,

                    ByteBufCodecs.VAR_LONG,
                    PlayerTransferPayload::amount,

                    PlayerTransferPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
