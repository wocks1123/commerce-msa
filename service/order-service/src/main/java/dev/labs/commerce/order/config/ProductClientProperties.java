package dev.labs.commerce.order.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "service.client.product")
@Getter
@Setter
public class ProductClientProperties {
    private String baseUrl;
    private int connectTimeout;
    private int readTimeout;
}
