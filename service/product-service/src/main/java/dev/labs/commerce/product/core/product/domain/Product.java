package dev.labs.commerce.product.core.product.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Entity
@Table(
        name = "product",
        indexes = {
                @Index(name = "idx_product_status", columnList = "product_status"),
                @Index(name = "idx_product_created_at", columnList = "created_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id", nullable = false, updatable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "price_amount", nullable = false)
    private Long price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 30)
    private ProductStatus productStatus;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;


    public static Product create(String name, long price, String currency, String description) {
        Assert.hasText(name, "productName must not be empty");
        Assert.isTrue(price >= 0, "price must be zero or greater");
        Assert.hasText(currency, "currency must not be empty");
        Assert.hasText(description, "description must not be empty");

        Product p = new Product();
        p.productName = name;
        p.price = price;
        p.currency = currency;
        p.description = description;
        p.productStatus = ProductStatus.INACTIVE; // 등록 후 활성화
        return p;
    }

    public void modify(String name, long priceAmount, String currency, String description) {
        if (this.productStatus == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "DISCONTINUED product cannot be updated.");
        }
        this.productName = name;
        this.price = priceAmount;
        this.currency = currency;
        this.description = description;
    }

    public void changeStatus(ProductStatus newStatus) {
        if (this.productStatus == ProductStatus.DISCONTINUED && newStatus != ProductStatus.DISCONTINUED) {
            throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "DISCONTINUED product cannot be reactivated.");
        }
        this.productStatus = newStatus;
    }

}
