package dev.labs.commerce.common.event;

import java.time.Instant;
import java.util.UUID;

public record EventEnvelope<T>(
        EventMeta meta,
        T payload
) {
    public static <T> EventEnvelope<T> of(
            T payload, Class<T> payloadType
    ) {
        EventMeta meta = new EventMeta(
                UUID.randomUUID().toString(),
                payloadType.getSimpleName(),
                Instant.now()
        );
        return new EventEnvelope<>(meta, payload);
    }
}
