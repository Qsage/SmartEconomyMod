
package com.qsage.economy.player.skin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerSkinRepository {

    void save(StoredPlayerSkin skin);

    Optional<StoredPlayerSkin> findByUuid(UUID uuid);

    List<StoredPlayerSkin> getAll();
}
