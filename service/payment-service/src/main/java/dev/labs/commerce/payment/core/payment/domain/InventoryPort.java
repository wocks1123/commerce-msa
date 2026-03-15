package dev.labs.commerce.payment.core.payment.domain;

import java.util.List;

public interface InventoryPort {

    void reserve(String orderId, List<Item> items);

    record Item(Long productId, int quantity) {}
}
