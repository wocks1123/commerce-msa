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

    @Column(name = "list_price", nullable = false)
    private Long listPrice;

    @Column(name = "selling_price", nullable = false)
    private Long sellingPrice;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable = false, length = 30)
    private ProductStatus productStatus;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;


    public static Product create(String name, long listPrice, long sellingPrice, String currency, String description) {
        Assert.hasText(name, "productName must not be empty");
        validatePrices(listPrice, sellingPrice);
        Assert.hasText(currency, "currency must not be empty");
        Assert.hasText(description, "description must not be empty");

        Product p = new Product();
        p.productName = name;
        p.listPrice = listPrice;
        p.sellingPrice = sellingPrice;
        p.currency = currency;
        p.description = description;
        p.productStatus = ProductStatus.DRAFT; // 등록 직후 검수/준비 상태
        return p;
    }

    public void modify(String name, long listPrice, long sellingPrice, String currency, String description) {
        if (this.productStatus == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "DISCONTINUED product cannot be updated.");
        }
        validatePrices(listPrice, sellingPrice);
        this.productName = name;
        this.listPrice = listPrice;
        this.sellingPrice = sellingPrice;
        this.currency = currency;
        this.description = description;
    }

    public void changeStatus(ProductStatus newStatus) {
        if (!this.productStatus.canTransitionTo(newStatus)) {
            throw new InvalidProductStatusException(
                    ProductErrorCode.INVALID_PRODUCT_STATUS,
                    "Cannot change product status from " + this.productStatus + " to " + newStatus + "."
            );
        }
        this.productStatus = newStatus;
    }

    private static void validatePrices(long listPrice, long sellingPrice) {
        Assert.isTrue(listPrice >= 0, "listPrice must be zero or greater");
        Assert.isTrue(sellingPrice >= 0, "sellingPrice must be zero or greater");
        Assert.isTrue(sellingPrice <= listPrice, "sellingPrice must be less than or equal to listPrice");
    }

}
