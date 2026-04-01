package dev.labs.commerce.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "order.expiry")
@Getter
@Setter
public class OrderExpiryProperties {
    private int pendingExpiryMinutes = 10;
}
