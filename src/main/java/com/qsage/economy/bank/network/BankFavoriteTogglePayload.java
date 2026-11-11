
package com.qsage.economy.bank.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record BankFavoriteTogglePayload(UUID target)
        implements CustomPacketPayload {

    public static final Type<BankFavoriteTogglePayload> TYPE =
            new Type<>(SmartEconomy.id("bank_favorite_toggle"));

    public static final StreamCodec<FriendlyByteBuf, BankFavoriteTogglePayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeUUID(payload.target()),
                    buf -> new BankFavoriteTogglePayload(buf.readUUID())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
