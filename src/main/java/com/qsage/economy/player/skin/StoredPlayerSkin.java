package com.qsage.economy.player.skin;

import java.util.UUID;

public record StoredPlayerSkin(
        UUID playerUuid,
        String textureValue,
        String textureSignature,
        long updatedAt
) {
}
