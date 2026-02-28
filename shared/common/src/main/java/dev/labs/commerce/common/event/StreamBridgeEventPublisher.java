package dev.labs.commerce.common.event;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class StreamBridgeEventPublisher implements EventPublisher {

    private final StreamBridge streamBridge;


    @Override
    public <T> void publish(String bindingName, String messageKey, EventEnvelope<T> envelope) {
        Message<EventEnvelope<T>> msg = MessageBuilder
                .withPayload(envelope)
                .setHeader(KafkaHeaders.KEY, messageKey.getBytes(StandardCharsets.UTF_8))
                .build();

        boolean sent = streamBridge.send(bindingName, msg);
        if (!sent) {
            throw new EventPublishException("Failed to publish event. binding=" + bindingName
                                            + ", eventId=" + envelope.meta().eventId()
                                            + ", eventType=" + envelope.meta().eventType());
        }
    }

}
