package com.qsage.economy.player;

import java.util.UUID;

public record KnownPlayer (
        UUID uuid,
        String name,
        long firstSeen,
        long lastSeen,
        String profileData
){
}
