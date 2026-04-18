package dev.labs.commerce.product.core.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static Product newDraftProduct() {
        return Product.create(
                "상품명", 10000L, 9000L, "KRW",
                ProductCategory.FIGURE, null, null, null, "설명"
        );
    }

    @Nested
    @DisplayName("상품 등록")
    class Create {

        @Test
        @DisplayName("유효한 정보로 상품을 등록하면 DRAFT 상태로 생성된다")
        void createProduct_withValidInfo_startsAsDraft() {
            Instant saleStart = Instant.now().plus(1, ChronoUnit.DAYS);
            Instant saleEnd = Instant.now().plus(10, ChronoUnit.DAYS);

            Product product = Product.create(
                    "테스트 상품", 10000L, 9000L, "KRW",
                    ProductCategory.PLUSH, saleStart, saleEnd, "https://cdn.example.com/thumb.jpg",
                    "테스트 상품 설명"
            );

            assertThat(product.getProductName()).isEqualTo("테스트 상품");
            assertThat(product.getListPrice()).isEqualTo(10000L);
            assertThat(product.getSellingPrice()).isEqualTo(9000L);
            assertThat(product.getCurrency()).isEqualTo("KRW");
            assertThat(product.getCategory()).isEqualTo(ProductCategory.PLUSH);
            assertThat(product.getSaleStartAt()).isEqualTo(saleStart);
            assertThat(product.getSaleEndAt()).isEqualTo(saleEnd);
            assertThat(product.getThumbnailUrl()).isEqualTo("https://cdn.example.com/thumb.jpg");
            assertThat(product.getDescription()).isEqualTo("테스트 상품 설명");
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DRAFT);
        }

        @Test
        @DisplayName("판매기간/썸네일이 null이어도 등록할 수 있다")
        void createProduct_withNullOptionalFields_succeeds() {
            Product product = Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.ETC, null, null, null, "설명"
            );

            assertThat(product.getSaleStartAt()).isNull();
            assertThat(product.getSaleEndAt()).isNull();
            assertThat(product.getThumbnailUrl()).isNull();
        }

        @Test
        @DisplayName("상품명이 없으면 등록할 수 없다")
        void createProduct_withBlankName_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    " ", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("정가가 음수이면 등록할 수 없다")
        void createProduct_withNegativeListPrice_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", -1L, 0L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매가가 음수이면 등록할 수 없다")
        void createProduct_withNegativeSellingPrice_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, -1L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매가가 정가보다 크면 등록할 수 없다")
        void createProduct_whenSellingPriceGreaterThanListPrice_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 11000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매가가 정가와 같으면 등록할 수 있다")
        void createProduct_whenSellingPriceEqualsListPrice_succeeds() {
            Product product = Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );

            assertThat(product.getListPrice()).isEqualTo(10000L);
            assertThat(product.getSellingPrice()).isEqualTo(10000L);
        }

        @Test
        @DisplayName("카테고리가 null이면 등록할 수 없다")
        void createProduct_withNullCategory_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    null, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매종료일이 과거이면 등록할 수 없다")
        void createProduct_whenSaleEndAtIsInPast_throwsException() {
            Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, past, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매시작일이 판매종료일보다 이후이면 등록할 수 없다")
        void createProduct_whenSaleStartAtAfterSaleEndAt_throwsException() {
            Instant start = Instant.now().plus(10, ChronoUnit.DAYS);
            Instant end = Instant.now().plus(5, ChronoUnit.DAYS);
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, start, end, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("판매시작일이 과거여도 등록할 수 있다")
        void createProduct_whenSaleStartAtIsInPast_succeeds() {
            Instant pastStart = Instant.now().minus(1, ChronoUnit.DAYS);
            Instant futureEnd = Instant.now().plus(5, ChronoUnit.DAYS);

            Product product = Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, pastStart, futureEnd, null, "설명"
            );

            assertThat(product.getSaleStartAt()).isEqualTo(pastStart);
        }

        @Test
        @DisplayName("썸네일 URL 길이가 500을 초과하면 등록할 수 없다")
        void createProduct_whenThumbnailUrlTooLong_throwsException() {
            String tooLong = "https://cdn.example.com/" + "a".repeat(500);
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, null, tooLong, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("통화 단위가 없으면 등록할 수 없다")
        void createProduct_withBlankCurrency_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, " ",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("상품 설명이 없으면 등록할 수 없다")
        void createProduct_withBlankDescription_throwsException() {
            assertThatThrownBy(() -> Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, " "
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("상품 정보 수정")
    class Modify {

        @Test
        @DisplayName("DRAFT 상품의 정보를 수정할 수 있다")
        void modifyProduct_whenDraft_succeeds() {
            Product product = newDraftProduct();
            Instant newStart = Instant.now().plus(2, ChronoUnit.DAYS);
            Instant newEnd = Instant.now().plus(20, ChronoUnit.DAYS);

            product.modify(
                    "새 상품명", 20000L, 18000L, "KRW",
                    ProductCategory.MODEL_KIT, newStart, newEnd, "https://cdn.example.com/new.jpg",
                    "새 설명"
            );

            assertThat(product.getProductName()).isEqualTo("새 상품명");
            assertThat(product.getListPrice()).isEqualTo(20000L);
            assertThat(product.getSellingPrice()).isEqualTo(18000L);
            assertThat(product.getCategory()).isEqualTo(ProductCategory.MODEL_KIT);
            assertThat(product.getSaleStartAt()).isEqualTo(newStart);
            assertThat(product.getSaleEndAt()).isEqualTo(newEnd);
            assertThat(product.getThumbnailUrl()).isEqualTo("https://cdn.example.com/new.jpg");
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("판매 종료된 상품은 정보를 수정할 수 없다")
        void modifyProduct_whenDiscontinued_throwsException() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.DISCONTINUED);

            assertThatThrownBy(() -> product.modify(
                    "새 상품명", 20000L, 18000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "새 설명"
            )).isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("수정 시 판매가가 정가보다 크면 예외가 발생한다")
        void modifyProduct_whenSellingPriceGreaterThanListPrice_throwsException() {
            Product product = newDraftProduct();

            assertThatThrownBy(() -> product.modify(
                    "상품명", 10000L, 11000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("수정 시 판매종료일이 과거이면 예외가 발생한다")
        void modifyProduct_whenSaleEndAtIsInPast_throwsException() {
            Product product = newDraftProduct();
            Instant past = Instant.now().minus(1, ChronoUnit.DAYS);

            assertThatThrownBy(() -> product.modify(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, past, null, "설명"
            )).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("모든 필드를 새 값으로 수정하면 변경된 필드 이름이 모두 반환된다")
        void modifyProduct_whenAllFieldsChanged_returnsAllFieldNames() {
            Product product = newDraftProduct();
            Instant newStart = Instant.now().plus(2, ChronoUnit.DAYS);
            Instant newEnd = Instant.now().plus(20, ChronoUnit.DAYS);

            Set<String> changedFields = product.modify(
                    "새 상품명", 20000L, 18000L, "USD",
                    ProductCategory.MODEL_KIT, newStart, newEnd, "https://cdn.example.com/new.jpg",
                    "새 설명"
            );

            assertThat(changedFields).containsExactlyInAnyOrder(
                    "productName", "listPrice", "sellingPrice", "currency",
                    "category", "saleStartAt", "saleEndAt", "thumbnailUrl", "description"
            );
        }

        @Test
        @DisplayName("동일한 값으로 수정하면 변경 필드셋이 비어있다")
        void modifyProduct_whenNoFieldChanged_returnsEmptySet() {
            Product product = Product.create(
                    "상품명", 10000L, 9000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );

            Set<String> changedFields = product.modify(
                    "상품명", 10000L, 9000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );

            assertThat(changedFields).isEmpty();
        }

        @Test
        @DisplayName("일부 필드만 변경되면 해당 필드 이름만 반환된다")
        void modifyProduct_whenSomeFieldsChanged_returnsOnlyChangedFieldNames() {
            Product product = Product.create(
                    "상품명", 10000L, 9000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );

            Set<String> changedFields = product.modify(
                    "새 상품명", 10000L, 8000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );

            assertThat(changedFields).containsExactlyInAnyOrder("productName", "sellingPrice");
        }

        @Test
        @DisplayName("nullable 필드를 null에서 값으로 바꾸면 변경으로 감지된다")
        void modifyProduct_whenNullableFieldChangesFromNullToValue_isDetected() {
            Product product = Product.create(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, null, null, null, "설명"
            );
            Instant newStart = Instant.now().plus(1, ChronoUnit.DAYS);
            Instant newEnd = Instant.now().plus(10, ChronoUnit.DAYS);

            Set<String> changedFields = product.modify(
                    "상품명", 10000L, 10000L, "KRW",
                    ProductCategory.FIGURE, newStart, newEnd, "https://cdn.example.com/thumb.jpg", "설명"
            );

            assertThat(changedFields).containsExactlyInAnyOrder("saleStartAt", "saleEndAt", "thumbnailUrl");
        }
    }

    @Nested
    @DisplayName("상품 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("DRAFT 상품을 판매 활성화할 수 있다")
        void changeStatus_fromDraftToActive_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("DRAFT 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromDraftToDiscontinued_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("DRAFT 상품을 INACTIVE로 바로 전이할 수 없다")
        void changeStatus_fromDraftToInactive_throwsException() {
            Product product = newDraftProduct();
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.INACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 중인 상품을 판매 중단할 수 있다")
        void changeStatus_fromActiveToInactive_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.INACTIVE);
        }

        @Test
        @DisplayName("판매 중인 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromActiveToDiscontinued_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("판매 중인 상품을 DRAFT로 되돌릴 수 없다")
        void changeStatus_fromActiveToDraft_throwsException() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 중단된 상품을 재판매할 수 있다")
        void changeStatus_fromInactiveToActive_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);
            product.changeStatus(ProductStatus.ACTIVE);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("판매 중단된 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromInactiveToDiscontinued_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("판매 중단된 상품을 DRAFT로 되돌릴 수 없다")
        void changeStatus_fromInactiveToDraft_throwsException() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료된 상품은 다시 판매 활성화할 수 없다")
        void changeStatus_fromDiscontinuedToActive_throwsException() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.ACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료된 상품은 비활성으로도 되돌릴 수 없다")
        void changeStatus_fromDiscontinuedToInactive_throwsException() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.INACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료 상태를 판매 종료로 다시 설정해도 예외가 발생하지 않는다")
        void changeStatus_fromDiscontinuedToDiscontinued_succeeds() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.DISCONTINUED);
            product.changeStatus(ProductStatus.DISCONTINUED);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }
    }

    @Nested
    @DisplayName("판매 스케줄 조회")
    class SaleSchedule {

        @Test
        @DisplayName("INACTIVE + 판매시작일이 미래면 isScheduled는 true")
        void isScheduled_whenInactiveAndStartInFuture_returnsTrue() {
            Product product = Product.create(
                    "상품명", 10000L, 9000L, "KRW",
                    ProductCategory.FIGURE,
                    Instant.now().plus(1, ChronoUnit.DAYS),
                    Instant.now().plus(10, ChronoUnit.DAYS),
                    null, "설명"
            );
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            assertThat(product.isScheduled()).isTrue();
            assertThat(product.isSaleExpired()).isFalse();
        }

        @Test
        @DisplayName("ACTIVE 상태에서는 isScheduled/isSaleExpired 모두 false")
        void isScheduledAndExpired_whenActive_returnsFalse() {
            Product product = Product.create(
                    "상품명", 10000L, 9000L, "KRW",
                    ProductCategory.FIGURE,
                    Instant.now().plus(1, ChronoUnit.DAYS),
                    Instant.now().plus(10, ChronoUnit.DAYS),
                    null, "설명"
            );
            product.changeStatus(ProductStatus.ACTIVE);

            assertThat(product.isScheduled()).isFalse();
            assertThat(product.isSaleExpired()).isFalse();
        }

        @Test
        @DisplayName("판매시작일이 null이면 isScheduled는 false")
        void isScheduled_whenSaleStartAtIsNull_returnsFalse() {
            Product product = newDraftProduct();
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            assertThat(product.isScheduled()).isFalse();
        }
    }
}
