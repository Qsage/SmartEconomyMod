package com.qsage.economy.transaction.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TransactionHistoryRequestPayload(
        int page
) implements CustomPacketPayload {

    public static final Type<TransactionHistoryRequestPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            SmartEconomy.MOD_ID,
                            "transaction_history_request"
                    )
            );

    public static final StreamCodec<FriendlyByteBuf, TransactionHistoryRequestPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> buf.writeVarInt(payload.page()),
                    buf -> new TransactionHistoryRequestPayload(
                            buf.readVarInt()
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}