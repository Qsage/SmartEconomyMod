
package com.qsage.economy.player.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SkinSyncPayload(
        UUID uuid,
        byte[] png
) implements CustomPacketPayload {

    public static final int MAX_BYTES = 128 * 1024;

    public static final Type<SkinSyncPayload> TYPE =
            new Type<>(SmartEconomy.id("skin_sync"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            SkinSyncPayload
            > CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeLong(
                        payload.uuid().getMostSignificantBits()
                );
                buf.writeLong(
                        payload.uuid().getLeastSignificantBits()
                );
                buf.writeByteArray(payload.png());
            },
            buf -> {
                UUID uuid = new UUID(
                        buf.readLong(),
                        buf.readLong()
                );

                byte[] png = buf.readByteArray(MAX_BYTES);

                return new SkinSyncPayload(uuid, png);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
