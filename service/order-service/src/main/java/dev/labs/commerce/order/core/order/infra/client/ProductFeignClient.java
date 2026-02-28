package dev.labs.commerce.order.core.order.infra.client;

import dev.labs.commerce.order.core.order.infra.client.dto.ProductSummaryDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", url = "${client.product-service.url}")
public interface ProductFeignClient {

    @GetMapping("/api/v1/products")
    List<ProductSummaryDto> listByIds(@RequestParam("ids") List<Long> ids);
}
