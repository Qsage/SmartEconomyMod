
package com.qsage.economy.notification.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OfflineNotificationsPayload(
        long lastNotificationId,
        long totalAmount,
        int transferCount
) implements CustomPacketPayload {

    public static final Type<OfflineNotificationsPayload> TYPE =
            new Type<>(SmartEconomy.id("offline_notifications"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OfflineNotificationsPayload
            > CODEC = new StreamCodec<>() {

        @Override
        public OfflineNotificationsPayload decode(
                RegistryFriendlyByteBuf buf
        ) {
            return new OfflineNotificationsPayload(
                    buf.readLong(),
                    buf.readLong(),
                    buf.readInt()
            );
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buf,
                OfflineNotificationsPayload payload
        ) {
            buf.writeLong(payload.lastNotificationId());
            buf.writeLong(payload.totalAmount());
            buf.writeInt(payload.transferCount());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
