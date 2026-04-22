package dev.labs.commerce.product.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "product.activation")
@Getter
@Setter
public class ProductActivationProperties {
    private int batchSize = 100;
}
