
package com.qsage.economy.player.skin;

import java.util.Optional;
import java.util.UUID;

public final class ServerPlayerSkinService {

    private final PlayerSkinRepository repository;

    public ServerPlayerSkinService(
            PlayerSkinRepository repository
    ) {
        this.repository = repository;
    }

    public void saveSkin(
            UUID playerUuid,
            String textureValue,
            String textureSignature
    ) {
        if (textureValue == null || textureValue.isBlank()) {
            return;
        }

        Optional<StoredPlayerSkin> existing =
                repository.findByUuid(playerUuid);

        if (existing.isPresent()) {
            StoredPlayerSkin previous = existing.get();

            if (previous.textureValue().equals(textureValue)
                    && java.util.Objects.equals(
                    previous.textureSignature(),
                    textureSignature
            )) {
                return;
            }
        }

        repository.save(
                new StoredPlayerSkin(
                        playerUuid,
                        textureValue,
                        textureSignature,
                        System.currentTimeMillis()
                )
        );
    }

    public Optional<StoredPlayerSkin> getSkin(UUID uuid) {
        return repository.findByUuid(uuid);
    }
}
