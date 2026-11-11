
package com.qsage.economy.player;

import com.qsage.economy.player.network.KnownPlayersSnapshotPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class KnownPlayerService {

    private final KnownPlayerRepository repository;

    public KnownPlayerService(KnownPlayerRepository repository) {
        this.repository = repository;
    }

    public void playerJoined(ServerPlayer player) {
        long now = Instant.now().getEpochSecond();

        KnownPlayer existing = repository.get(player.getUUID());

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

    public KnownPlayer get(UUID uuid) {
        return repository.get(uuid);
    }

    public List<KnownPlayer> getAll() {
        return repository.getAll();
    }



    public KnownPlayersSnapshotPayload createSnapshot(
            MinecraftServer server
    ) {
        return createSnapshot(server, null);
    }

    public KnownPlayersSnapshotPayload createJoinSnapshot(
            MinecraftServer server,
            UUID joiningUuid
    ) {
        List<KnownPlayersSnapshotPayload.KnownPlayerData> players =
                repository.getAll()
                        .stream()
                        .map(player -> {

                            boolean online =
                                    player.uuid().equals(joiningUuid)
                                            || server.getPlayerList()
                                            .getPlayer(player.uuid()) != null;

                            return new KnownPlayersSnapshotPayload.KnownPlayerData(
                                    player.uuid(),
                                    player.name(),
                                    online
                            );
                        })
                        .toList();

        return new KnownPlayersSnapshotPayload(players);
    }

    public KnownPlayersSnapshotPayload createSnapshot(
            MinecraftServer server,
            UUID disconnectingUuid
    ) {
        List<KnownPlayersSnapshotPayload.KnownPlayerData> players =
                repository.getAll()
                        .stream()
                        .map(player -> {

                            boolean online =
                                    !player.uuid().equals(disconnectingUuid)
                                            && server.getPlayerList()
                                            .getPlayer(player.uuid()) != null;

                            return new KnownPlayersSnapshotPayload.KnownPlayerData(
                                    player.uuid(),
                                    player.name(),
                                    online
                            );
                        })
                        .toList();

        return new KnownPlayersSnapshotPayload(players);
    }


}
