package com.qsage.economy.transaction.network;

import com.qsage.SmartEconomy;
import com.qsage.economy.transaction.Transaction;
import com.qsage.economy.transaction.TransactionType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record TransactionHistorySnapshotPayload(
        int page,
        boolean hasNextPage,
        List<Transaction> transactions
) implements CustomPacketPayload {

    public static final Type<TransactionHistorySnapshotPayload> TYPE =
            new Type<>(
                    Identifier.fromNamespaceAndPath(
                            SmartEconomy.MOD_ID,
                            "transaction_history_snapshot"
                    )
            );

    public static final StreamCodec<FriendlyByteBuf, TransactionHistorySnapshotPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.page());
                        buf.writeBoolean(payload.hasNextPage());

                        buf.writeVarInt(payload.transactions().size());

                        for (Transaction transaction : payload.transactions()) {
                            buf.writeUUID(transaction.id());
                            buf.writeLong(transaction.timestamp().toEpochMilli());
                            buf.writeEnum(transaction.type());

                            writeNullableUuid(buf, transaction.actor());
                            writeNullableUuid(buf, transaction.target());

                            buf.writeLong(transaction.amount());
                            buf.writeUtf(
                                    transaction.reason() == null
                                            ? ""
                                            : transaction.reason()
                            );
                        }
                    },
                    buf -> {
                        int page = buf.readVarInt();
                        boolean hasNextPage = buf.readBoolean();

                        int count = buf.readVarInt();

                        if (count < 0 || count > 20) {
                            throw new IllegalArgumentException(
                                    "Invalid transaction history size: " + count
                            );
                        }

                        List<Transaction> transactions = new ArrayList<>(count);

                        for (int i = 0; i < count; i++) {
                            UUID id = buf.readUUID();
                            Instant timestamp = Instant.ofEpochMilli(buf.readLong());
                            TransactionType type = buf.readEnum(TransactionType.class);

                            UUID actor = readNullableUuid(buf);
                            UUID target = readNullableUuid(buf);

                            long amount = buf.readLong();
                            String reason = buf.readUtf(512);

                            transactions.add(
                                    new Transaction(
                                            id,
                                            timestamp,
                                            type,
                                            actor,
                                            target,
                                            amount,
                                            reason
                                    )
                            );
                        }

                        return new TransactionHistorySnapshotPayload(
                                page,
                                hasNextPage,
                                transactions
                        );
                    }
            );

    private static void writeNullableUuid(FriendlyByteBuf buf, UUID uuid) {
        buf.writeBoolean(uuid != null);

        if (uuid != null) {
            buf.writeUUID(uuid);
        }
    }

    private static UUID readNullableUuid(FriendlyByteBuf buf) {
        return buf.readBoolean() ? buf.readUUID() : null;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}