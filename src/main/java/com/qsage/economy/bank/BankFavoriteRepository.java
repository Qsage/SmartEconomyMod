
package com.qsage.economy.bank;

import java.util.Set;
import java.util.UUID;

public interface BankFavoriteRepository {

    Set<UUID> getFavorites(UUID owner);

    boolean isFavorite(UUID owner, UUID target);

    void addFavorite(UUID owner, UUID target);

    void removeFavorite(UUID owner, UUID target);
}
