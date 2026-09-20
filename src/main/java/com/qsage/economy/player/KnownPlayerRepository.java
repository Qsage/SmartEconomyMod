package com.qsage.economy.player;

import java.util.List;
import java.util.UUID;

public interface KnownPlayerRepository {

    void saverOrUpdate(KnownPlayer player);

    KnownPlayer get(UUID uuid);

    List<KnownPlayer> getAll();

    List<KnownPlayer> searchByName(String name);
}
