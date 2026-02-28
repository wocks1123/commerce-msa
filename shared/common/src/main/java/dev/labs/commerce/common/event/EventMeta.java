package dev.labs.commerce.common.event;

import java.time.Instant;

public record EventMeta(
        String eventId, // UUID
        String eventType,
        Instant occurredAt
) {
}
