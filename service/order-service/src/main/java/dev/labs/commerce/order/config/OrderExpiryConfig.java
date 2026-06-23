package dev.labs.commerce.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OrderExpiryProperties.class)
public class OrderExpiryConfig {
}
