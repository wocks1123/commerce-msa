package dev.labs.commerce.product.core.product.domain;

import dev.labs.commerce.common.entity.BaseEntity;
import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;
import dev.labs.commerce.product.core.product.domain.error.ProductErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.time.Instant;

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

    private static final int THUMBNAIL_URL_MAX_LENGTH = 500;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ProductCategory category;

    @Column(name = "sale_start_at")
    private Instant saleStartAt;

    @Column(name = "sale_end_at")
    private Instant saleEndAt;

    @Column(name = "thumbnail_url", length = THUMBNAIL_URL_MAX_LENGTH)
    private String thumbnailUrl;

    @Column(name = "description", nullable = false, columnDefinition = "text")
    private String description;


    public static Product create(
            String name,
            long listPrice,
            long sellingPrice,
            String currency,
            ProductCategory category,
            Instant saleStartAt,
            Instant saleEndAt,
            String thumbnailUrl,
            String description
    ) {
        Assert.hasText(name, "productName must not be empty");
        validatePrices(listPrice, sellingPrice);
        Assert.hasText(currency, "currency must not be empty");
        Assert.notNull(category, "category must not be null");
        validateSalePeriod(saleStartAt, saleEndAt);
        validateThumbnailUrl(thumbnailUrl);
        Assert.hasText(description, "description must not be empty");

        Product p = new Product();
        p.productName = name;
        p.listPrice = listPrice;
        p.sellingPrice = sellingPrice;
        p.currency = currency;
        p.category = category;
        p.saleStartAt = saleStartAt;
        p.saleEndAt = saleEndAt;
        p.thumbnailUrl = thumbnailUrl;
        p.description = description;
        p.productStatus = ProductStatus.DRAFT; // 등록 직후 검수/준비 상태
        return p;
    }

    public void modify(
            String name,
            long listPrice,
            long sellingPrice,
            String currency,
            ProductCategory category,
            Instant saleStartAt,
            Instant saleEndAt,
            String thumbnailUrl,
            String description
    ) {
        if (this.productStatus == ProductStatus.DISCONTINUED) {
            throw new InvalidProductStatusException(ProductErrorCode.INVALID_PRODUCT_STATUS, "DISCONTINUED product cannot be updated.");
        }
        validatePrices(listPrice, sellingPrice);
        Assert.notNull(category, "category must not be null");
        validateSalePeriod(saleStartAt, saleEndAt);
        validateThumbnailUrl(thumbnailUrl);

        this.productName = name;
        this.listPrice = listPrice;
        this.sellingPrice = sellingPrice;
        this.currency = currency;
        this.category = category;
        this.saleStartAt = saleStartAt;
        this.saleEndAt = saleEndAt;
        this.thumbnailUrl = thumbnailUrl;
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

    public boolean isScheduled() {
        return this.productStatus == ProductStatus.INACTIVE
                && this.saleStartAt != null
                && this.saleStartAt.isAfter(Instant.now());
    }

    public boolean isSaleExpired() {
        return this.productStatus == ProductStatus.INACTIVE
                && this.saleEndAt != null
                && this.saleEndAt.isBefore(Instant.now());
    }

    private static void validatePrices(long listPrice, long sellingPrice) {
        Assert.isTrue(listPrice >= 0, "listPrice must be zero or greater");
        Assert.isTrue(sellingPrice >= 0, "sellingPrice must be zero or greater");
        Assert.isTrue(sellingPrice <= listPrice, "sellingPrice must be less than or equal to listPrice");
    }

    private static void validateSalePeriod(Instant saleStartAt, Instant saleEndAt) {
        if (saleEndAt != null && saleEndAt.isBefore(Instant.now())) {
            throw new IllegalArgumentException("saleEndAt must not be in the past");
        }
        if (saleStartAt != null && saleEndAt != null && !saleStartAt.isBefore(saleEndAt)) {
            throw new IllegalArgumentException("saleStartAt must be before saleEndAt");
        }
    }

    private static void validateThumbnailUrl(String thumbnailUrl) {
        if (thumbnailUrl != null && thumbnailUrl.length() > THUMBNAIL_URL_MAX_LENGTH) {
            throw new IllegalArgumentException("thumbnailUrl must not exceed " + THUMBNAIL_URL_MAX_LENGTH + " characters");
        }
    }

}
