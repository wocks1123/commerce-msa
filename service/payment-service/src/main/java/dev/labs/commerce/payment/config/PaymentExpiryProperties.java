package dev.labs.commerce.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "payment.expiry")
@Getter
@Setter
public class PaymentExpiryProperties {
    private int requestedExpiryMinutes = 30;
}
