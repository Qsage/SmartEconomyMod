package com.qsage.economy.transaction;

import java.time.Instant;
import java.util.UUID;

public record Transaction (
        UUID id,
        Instant timestamp,
        TransactionType type,
        UUID actor,
        UUID target,
        long amount,
        String reason
){

}
