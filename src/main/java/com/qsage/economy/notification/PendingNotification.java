
package com.qsage.economy.notification;

import java.util.UUID;

public record PendingNotification(
        long id,
        UUID playerUuid,
        UUID senderUuid,
        long amount,
        long createdAt
) {
}
