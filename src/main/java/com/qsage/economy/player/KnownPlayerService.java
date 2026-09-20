package com.qsage.economy.player;

import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;

public class KnownPlayerService {

    private final KnownPlayerRepository repository;

    public KnownPlayerService(KnownPlayerRepository repository) {
        this.repository = repository;
    }

    public void playerJoined(ServerPlayer player) {
        long now = Instant.now().getEpochSecond();

        KnownPlayer existing =
                repository.get(player.getUUID());

        long firstSeen = existing != null
                ? existing.firstSeen()
                : now;

        String profileData = existing != null
                ? existing.profileData()
                : null;

        KnownPlayer newPlayer = new KnownPlayer(
                player.getUUID(),
                player.getGameProfile().name(),
                firstSeen,
                now,
                profileData
        );

        repository.saverOrUpdate(newPlayer);
    }

    public KnownPlayer get(java.util.UUID uuid) {
        return repository.get(uuid);
    }
}
