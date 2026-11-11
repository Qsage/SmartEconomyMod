package com.qsage.economy.npc.dialogue;

import com.qsage.economy.npc.EconomyNpcEntity;
import com.qsage.economy.npc.dialogue.network.NpcDialogueOpenPayload;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

public final class NpcDialogueService {

    private static final double MAX_DISTANCE_SQR = 64.0;

    private NpcDialogueService() {
    }

    public static void open(
            ServerPlayer player,
            EconomyNpcEntity npc
    ) {
        if (!canInteract(player, npc)) {
            return;
        }

        send(
                player,
                npc,
                "Приветствую, путешественник! Чем могу помочь?",
                true
        );
    }

    public static void handleAction(
            ServerPlayer player,
            int npcId,
            int optionId
    ) {
        var entity = player.level().getEntity(npcId);

        if (!(entity instanceof EconomyNpcEntity npc)) {
            return;
        }

        if (!canInteract(player, npc)) {
            return;
        }

        String response = switch (optionId) {
            case 1 -> "Скоро здесь появятся задания.";
            default -> null;
        };

        if (response == null) {
            return;
        }

        send(player, npc, response, false);
    }

    private static boolean canInteract(
            ServerPlayer player,
            EconomyNpcEntity npc
    ) {
        return npc.isAlive()
                && player.level() == npc.level()
                && player.distanceToSqr(npc) <= MAX_DISTANCE_SQR;
    }

    private static void send(
            ServerPlayer player,
            EconomyNpcEntity npc,
            String message,
            boolean showOptions
    ) {
        ServerPlayNetworking.send(
                player,
                new NpcDialogueOpenPayload(
                        npc.getId(),
                        "Торговец",
                        message,
                        showOptions
                )
        );
    }
}