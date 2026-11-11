package com.qsage.economy.npc.dialogue.network;

import com.qsage.SmartEconomy;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record NpcDialogueActionPayload(
        int npcId,
        int optionId
) implements CustomPacketPayload {

    public static final Type<NpcDialogueActionPayload> TYPE =
            new Type<>(SmartEconomy.id("npc_dialogue_action"));

    public static final StreamCodec<
            RegistryFriendlyByteBuf,
            NpcDialogueActionPayload
            > CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.npcId());
                buf.writeVarInt(payload.optionId());
            },
            buf -> new NpcDialogueActionPayload(
                    buf.readVarInt(),
                    buf.readVarInt()
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}