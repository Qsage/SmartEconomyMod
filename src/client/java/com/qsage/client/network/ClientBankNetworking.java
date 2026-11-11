
package com.qsage.client.network;

import com.qsage.economy.bank.network.BankFavoriteTogglePayload;
import com.qsage.economy.player.network.KnownPlayersRequestPayload;
import com.qsage.economy.player.network.PlayerTransferPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.UUID;

public final class ClientBankNetworking {

    private ClientBankNetworking() {
    }

    public static void requestPlayers() {
        if (ClientPlayNetworking.canSend(
                KnownPlayersRequestPayload.TYPE
        )) {
            ClientPlayNetworking.send(
                    new KnownPlayersRequestPayload()
            );
        }
    }


    public static void toggleFavorite(UUID target) {
        if (ClientPlayNetworking.canSend(
                BankFavoriteTogglePayload.TYPE
        )) {
            ClientPlayNetworking.send(
                    new BankFavoriteTogglePayload(target)
            );
        }
    }

    public static boolean transfer(UUID target, long amount) {
        if (target == null || amount <= 0) {
            return false;
        }

        if (!ClientPlayNetworking.canSend(PlayerTransferPayload.TYPE)) {
            return false;
        }

        ClientPlayNetworking.send(
                new PlayerTransferPayload(target, amount)
        );

        return true;
    }

}
