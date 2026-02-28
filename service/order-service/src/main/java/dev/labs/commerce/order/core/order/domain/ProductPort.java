package dev.labs.commerce.order.core.order.domain;

import java.util.List;

public interface ProductPort {

    List<ProductInfo> findProducts(List<Long> productIds);

}
