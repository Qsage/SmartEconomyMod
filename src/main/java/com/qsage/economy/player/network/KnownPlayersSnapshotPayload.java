package com.qsage.economy.player.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record KnownPlayersSnapshotPayload(
        List<KnownPlayerData> players
) implements CustomPacketPayload {

    public static final Type<KnownPlayersSnapshotPayload> TYPE =
            new Type<>(
                    SmartEconomy.id("known_players_snapshot")
            );

    public static final StreamCodec<RegistryFriendlyByteBuf, KnownPlayersSnapshotPayload> CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.collection(
                            ArrayList::new,
                            KnownPlayerData.CODEC
                    ),
                    KnownPlayersSnapshotPayload::players,
                    KnownPlayersSnapshotPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public record KnownPlayerData(
            UUID uuid,
            String name,
            boolean online
    ) {

        private static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID_STREAM_CODEC =
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

        public static final StreamCodec<RegistryFriendlyByteBuf, KnownPlayerData> CODEC =
                StreamCodec.composite(
                        UUID_STREAM_CODEC,
                        KnownPlayerData::uuid,

                        ByteBufCodecs.STRING_UTF8,
                        KnownPlayerData::name,

                        ByteBufCodecs.BOOL,
                        KnownPlayerData::online,

                        KnownPlayerData::new
                );
    }

}