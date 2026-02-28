package dev.labs.commerce.product.core.product.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByProductIdIn(List<Long> productIds);
}
