package dev.labs.commerce.common.event;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class EventCoreAutoConfiguration {

    @Bean
    public EventPublisher eventPublisher(StreamBridge streamBridge) {
        return new StreamBridgeEventPublisher(streamBridge);
    }

}
