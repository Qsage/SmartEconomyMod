
package com.qsage.economy.bank.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public record BankFavoritesSnapshotPayload(Set<UUID> favorites)
        implements CustomPacketPayload {

    private static final int MAX_FAVORITES = 4096;

    public static final Type<BankFavoritesSnapshotPayload> TYPE =
            new Type<>(SmartEconomy.id("bank_favorites_snapshot"));

    public static final StreamCodec<FriendlyByteBuf, BankFavoritesSnapshotPayload> CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.favorites().size());

                        for (UUID uuid : payload.favorites()) {
                            buf.writeUUID(uuid);
                        }
                    },
                    buf -> {
                        int count = buf.readVarInt();

                        if (count < 0 || count > MAX_FAVORITES) {
                            throw new IllegalArgumentException(
                                    "Invalid bank favorites count: " + count
                            );
                        }

                        Set<UUID> favorites = new HashSet<>();

                        for (int i = 0; i < count; i++) {
                            favorites.add(buf.readUUID());
                        }

                        return new BankFavoritesSnapshotPayload(favorites);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
