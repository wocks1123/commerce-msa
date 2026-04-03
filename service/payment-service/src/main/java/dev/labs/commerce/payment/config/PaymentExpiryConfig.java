package dev.labs.commerce.payment.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(PaymentExpiryProperties.class)
@EnableScheduling
public class PaymentExpiryConfig {
}
