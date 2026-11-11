
package com.qsage.client.player;

import com.qsage.economy.bank.network.BankFavoritesSnapshotPayload;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ClientBankFavorites {

    private static final Set<UUID> FAVORITES = new HashSet<>();

    private ClientBankFavorites() {
    }

    public static void update(BankFavoritesSnapshotPayload payload) {
        FAVORITES.clear();
        FAVORITES.addAll(payload.favorites());
    }

    public static boolean isFavorite(UUID uuid) {
        return FAVORITES.contains(uuid);
    }

    public static void clear() {
        FAVORITES.clear();
    }
}
