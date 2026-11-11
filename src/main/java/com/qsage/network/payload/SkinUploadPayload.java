
package com.qsage.network.payload;

import com.qsage.SmartEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SkinUploadPayload(byte[] png)
        implements CustomPacketPayload {

    public static final int MAX_BYTES = 128 * 1024;

    public static final Type<SkinUploadPayload> TYPE =
            new Type<>(SmartEconomy.id("skin_upload"));

    public static final StreamCodec<FriendlyByteBuf, SkinUploadPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeByteArray(payload.png()),
                    buf -> new SkinUploadPayload(
                            buf.readByteArray(MAX_BYTES)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
