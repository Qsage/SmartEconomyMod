package com.qsage.client.gui.screen;

import java.util.UUID;

public record BankPlayer(
        UUID uuid,
        String name,
        boolean online,
        boolean favorite
){
}
