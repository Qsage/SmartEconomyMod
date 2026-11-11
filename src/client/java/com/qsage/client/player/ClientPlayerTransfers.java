package com.qsage.client.player;

import com.qsage.economy.player.network.PlayerTransferPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.UUID;

public final class ClientPlayerTransfers {
    private ClientPlayerTransfers() {
    }

    public static void send(UUID target, long amount) {
        if(target == null) {
            throw new IllegalArgumentException("Target cannot be null");
        }

        if(amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        ClientPlayNetworking.send(
                new PlayerTransferPayload(target, amount)
        );
    }
}
