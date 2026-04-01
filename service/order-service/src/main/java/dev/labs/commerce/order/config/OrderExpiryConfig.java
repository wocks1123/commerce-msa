package dev.labs.commerce.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableConfigurationProperties(OrderExpiryProperties.class)
@EnableScheduling
public class OrderExpiryConfig {
}
