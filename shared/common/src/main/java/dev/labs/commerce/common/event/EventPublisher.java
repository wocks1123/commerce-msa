package dev.labs.commerce.common.event;

public interface EventPublisher {

    <T> void publish(String bindingName, String messageKey, EventEnvelope<T> envelope);

}
