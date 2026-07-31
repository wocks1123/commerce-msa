package dev.labs.commerce.payment.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.client.order")
@Getter
@Setter
public class OrderClientProperties {
    private String baseUrl;
    private int connectTimeout;
    private int readTimeout;
}
