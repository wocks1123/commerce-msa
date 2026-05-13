package dev.labs.commerce.product.core.product.domain.fixture;

import dev.labs.commerce.product.core.product.domain.Product;
import dev.labs.commerce.product.core.product.domain.ProductCategory;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import org.springframework.beans.BeanUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;

public class ProductFixture {

    private Long productId;
    private String productName;
    private long listPrice;
    private long sellingPrice;
    private String currency;
    private ProductStatus productStatus;
    private ProductCategory category;
    private Instant saleStartAt;
    private Instant saleEndAt;
    private String thumbnailUrl;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    private ProductFixture() {
    }

    public static ProductFixture builder() {
        return new ProductFixture();
    }

    public ProductFixture productId(Long productId) {
        this.productId = productId;
        return this;
    }

    public ProductFixture productName(String productName) {
        this.productName = productName;
        return this;
    }

    public ProductFixture listPrice(long listPrice) {
        this.listPrice = listPrice;
        return this;
    }

    public ProductFixture sellingPrice(long sellingPrice) {
        this.sellingPrice = sellingPrice;
        return this;
    }

    public ProductFixture currency(String currency) {
        this.currency = currency;
        return this;
    }

    public ProductFixture productStatus(ProductStatus productStatus) {
        this.productStatus = productStatus;
        return this;
    }

    public ProductFixture category(ProductCategory category) {
        this.category = category;
        return this;
    }

    public ProductFixture saleStartAt(Instant saleStartAt) {
        this.saleStartAt = saleStartAt;
        return this;
    }

    public ProductFixture saleEndAt(Instant saleEndAt) {
        this.saleEndAt = saleEndAt;
        return this;
    }

    public ProductFixture thumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
        return this;
    }

    public ProductFixture description(String description) {
        this.description = description;
        return this;
    }

    public ProductFixture createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProductFixture updatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public ProductFixture withSample() {
        this.productId = 1L;
        this.productName = "상품명";
        this.listPrice = 10000L;
        this.sellingPrice = 9000L;
        this.currency = "KRW";
        this.productStatus = ProductStatus.DRAFT;
        this.category = ProductCategory.FIGURE;
        this.description = "설명";
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        return this;
    }

    public Product build() {
        final Product product = BeanUtils.instantiateClass(Product.class);
        ReflectionTestUtils.setField(product, "productId", productId);
        ReflectionTestUtils.setField(product, "productName", productName);
        ReflectionTestUtils.setField(product, "listPrice", listPrice);
        ReflectionTestUtils.setField(product, "sellingPrice", sellingPrice);
        ReflectionTestUtils.setField(product, "currency", currency);
        ReflectionTestUtils.setField(product, "productStatus", productStatus);
        ReflectionTestUtils.setField(product, "category", category);
        ReflectionTestUtils.setField(product, "saleStartAt", saleStartAt);
        ReflectionTestUtils.setField(product, "saleEndAt", saleEndAt);
        ReflectionTestUtils.setField(product, "thumbnailUrl", thumbnailUrl);
        ReflectionTestUtils.setField(product, "description", description);
        ReflectionTestUtils.setField(product, "createdAt", createdAt);
        ReflectionTestUtils.setField(product, "updatedAt", updatedAt);
        return product;
    }
}
