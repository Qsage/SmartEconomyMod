
package com.qsage.economy.notification.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record OfflineNotificationsAckPayload(
        long lastNotificationId
) implements CustomPacketPayload {

    public static final Type<OfflineNotificationsAckPayload> TYPE =
            new Type<>(SmartEconomy.id("offline_notifications_ack"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            OfflineNotificationsAckPayload
            > CODEC = new StreamCodec<>() {

        @Override
        public OfflineNotificationsAckPayload decode(
                RegistryFriendlyByteBuf buf
        ) {
            return new OfflineNotificationsAckPayload(
                    buf.readLong()
            );
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buf,
                OfflineNotificationsAckPayload payload
        ) {
            buf.writeLong(payload.lastNotificationId());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
