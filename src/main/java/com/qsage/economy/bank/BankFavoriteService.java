
package com.qsage.economy.bank;

import java.util.Set;
import java.util.UUID;

public final class BankFavoriteService {

    private final BankFavoriteRepository repository;

    public BankFavoriteService(BankFavoriteRepository repository) {
        this.repository = repository;
    }

    public Set<UUID> getFavorites(UUID owner) {
        return repository.getFavorites(owner);
    }

    public boolean toggle(UUID owner, UUID target) {
        if (owner.equals(target)) {
            throw new IllegalArgumentException(
                    "Cannot add yourself to bank favorites"
            );
        }

        if (repository.isFavorite(owner, target)) {
            repository.removeFavorite(owner, target);
            return false;
        }

        repository.addFavorite(owner, target);
        return true;
    }
}
