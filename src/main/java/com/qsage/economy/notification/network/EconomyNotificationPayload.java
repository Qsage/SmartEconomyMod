
package com.qsage.economy.notification.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record EconomyNotificationPayload(
        String notificationType,
        String titleKey,
        String messageKey,
        String argument
) implements CustomPacketPayload {

    public static final Type<EconomyNotificationPayload> TYPE =
            new Type<>(
                    SmartEconomy.id("economy_notification")
            );

    private static final StreamCodec<
            RegistryFriendlyByteBuf,
            String
            > STRING_CODEC = new StreamCodec<>() {

        @Override
        public String decode(RegistryFriendlyByteBuf buf) {
            return ByteBufCodecs.STRING_UTF8.decode(buf);
        }

        @Override
        public void encode(
                RegistryFriendlyByteBuf buf,
                String value
        ) {
            ByteBufCodecs.STRING_UTF8.encode(buf, value);
        }
    };

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            EconomyNotificationPayload
            > CODEC = StreamCodec.composite(
            STRING_CODEC,
            EconomyNotificationPayload::notificationType,

            STRING_CODEC,
            EconomyNotificationPayload::titleKey,

            STRING_CODEC,
            EconomyNotificationPayload::messageKey,

            STRING_CODEC,
            EconomyNotificationPayload::argument,

            EconomyNotificationPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
