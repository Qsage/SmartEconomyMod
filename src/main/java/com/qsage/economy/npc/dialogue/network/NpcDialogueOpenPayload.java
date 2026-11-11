package com.qsage.economy.npc.dialogue.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NpcDialogueOpenPayload(
        int npcId,
        String npcName,
        String message,
        boolean showOptions
) implements CustomPacketPayload {

    public static final Type<NpcDialogueOpenPayload> TYPE =
            new Type<>(SmartEconomy.id("npc_dialogue_open"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            NpcDialogueOpenPayload
            > CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.npcId());
                buf.writeUtf(payload.npcName(), 128);
                buf.writeUtf(payload.message(), 1024);
                buf.writeBoolean(payload.showOptions());
            },
            buf -> new NpcDialogueOpenPayload(
                    buf.readVarInt(),
                    buf.readUtf(128),
                    buf.readUtf(1024),
                    buf.readBoolean()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}