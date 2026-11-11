package com.qsage.client.player;

import com.qsage.economy.player.network.KnownPlayersSnapshotPayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientKnownPlayers {

    private static final Map<UUID, KnownPlayersSnapshotPayload.KnownPlayerData> KNOWN_PLAYERS =
            new HashMap<>();

    private ClientKnownPlayers() {
    }

    public static void setPlayers(List<KnownPlayersSnapshotPayload.KnownPlayerData> players) {
        KNOWN_PLAYERS.clear();

        for (KnownPlayersSnapshotPayload.KnownPlayerData player : players) {
            KNOWN_PLAYERS.put(player.uuid(), player);
        }
    }

    public static void update(KnownPlayersSnapshotPayload payload) {
        KNOWN_PLAYERS.clear();

        for (KnownPlayersSnapshotPayload.KnownPlayerData player : payload.players()) {
            KNOWN_PLAYERS.put(player.uuid(), player);
        }
    }

    public static List<KnownPlayersSnapshotPayload.KnownPlayerData> getAll() {
        return new ArrayList<>(KNOWN_PLAYERS.values());
    }

    public static KnownPlayersSnapshotPayload.KnownPlayerData get(UUID uuid) {
        return KNOWN_PLAYERS.get(uuid);
    }

    public static List<KnownPlayersSnapshotPayload.KnownPlayerData> search(String query) {

        if (query == null || query.isBlank()) {
            return getAll();
        }

        String normalized = query.toLowerCase();

        return KNOWN_PLAYERS.values()
                .stream()
                .filter(player ->
                        player.name().toLowerCase().contains(normalized)
                )
                .toList();
    }

    public static void clear() {
        KNOWN_PLAYERS.clear();
    }
}