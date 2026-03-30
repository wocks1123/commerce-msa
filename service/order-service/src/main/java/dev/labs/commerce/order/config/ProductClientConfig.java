package dev.labs.commerce.order.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ProductClientProperties.class)
public class ProductClientConfig {

    @Bean
    public RestClient productRestClient(RestClient.Builder builder, ProductClientProperties properties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        return builder
                .baseUrl(properties.getBaseUrl())
                .requestFactory(factory)
                .build();
    }
}
