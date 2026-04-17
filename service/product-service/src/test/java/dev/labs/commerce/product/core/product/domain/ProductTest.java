package dev.labs.commerce.product.core.product.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.labs.commerce.product.core.product.domain.error.InvalidProductStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    @Nested
    @DisplayName("상품 등록")
    class Create {

        @Test
        @DisplayName("유효한 정보로 상품을 등록하면 DRAFT 상태로 생성된다")
        void createProduct_withValidInfo_startsAsDraft() {
            // given
            String name = "테스트 상품";
            long price = 10000L;
            String currency = "KRW";
            String description = "테스트 상품 설명";

            // when
            Product product = Product.create(name, price, currency, description);

            // then
            assertThat(product.getProductName()).isEqualTo(name);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getCurrency()).isEqualTo(currency);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DRAFT);
        }

        @Test
        @DisplayName("상품명이 없으면 등록할 수 없다")
        void createProduct_withBlankName_throwsException() {
            // given
            String blankName = " ";

            // when & then
            assertThatThrownBy(() -> Product.create(blankName, 10000L, "KRW", "설명"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("가격이 음수이면 등록할 수 없다")
        void createProduct_withNegativePrice_throwsException() {
            // given
            long negativePrice = -1L;

            // when & then
            assertThatThrownBy(() -> Product.create("상품명", negativePrice, "KRW", "설명"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("가격이 0원인 상품은 등록할 수 있다")
        void createProduct_withZeroPrice_succeeds() {
            // given
            long zeroPrice = 0L;

            // when
            Product product = Product.create("상품명", zeroPrice, "KRW", "설명");

            // then
            assertThat(product.getPrice()).isZero();
        }

        @Test
        @DisplayName("통화 단위가 없으면 등록할 수 없다")
        void createProduct_withBlankCurrency_throwsException() {
            // given
            String blankCurrency = " ";

            // when & then
            assertThatThrownBy(() -> Product.create("상품명", 10000L, blankCurrency, "설명"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("상품 설명이 없으면 등록할 수 없다")
        void createProduct_withBlankDescription_throwsException() {
            // given
            String blankDescription = " ";

            // when & then
            assertThatThrownBy(() -> Product.create("상품명", 10000L, "KRW", blankDescription))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("상품 정보 수정")
    class Modify {

        @Test
        @DisplayName("DRAFT 상품의 정보를 수정할 수 있다")
        void modifyProduct_whenDraft_succeeds() {
            // given
            Product product = Product.create("기존 상품명", 10000L, "KRW", "기존 설명");

            // when
            product.modify("새 상품명", 20000L, "KRW", "새 설명");

            // then
            assertThat(product.getProductName()).isEqualTo("새 상품명");
            assertThat(product.getPrice()).isEqualTo(20000L);
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("판매 중인 상품의 정보를 수정할 수 있다")
        void modifyProduct_whenActive_succeeds() {
            // given
            Product product = Product.create("기존 상품명", 10000L, "KRW", "기존 설명");
            product.changeStatus(ProductStatus.ACTIVE);

            // when
            product.modify("새 상품명", 20000L, "KRW", "새 설명");

            // then
            assertThat(product.getProductName()).isEqualTo("새 상품명");
            assertThat(product.getPrice()).isEqualTo(20000L);
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("판매 중단된 상품의 정보도 수정할 수 있다")
        void modifyProduct_whenInactive_succeeds() {
            // given
            Product product = Product.create("기존 상품명", 10000L, "KRW", "기존 설명");
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            // when
            product.modify("새 상품명", 20000L, "KRW", "새 설명");

            // then
            assertThat(product.getProductName()).isEqualTo("새 상품명");
            assertThat(product.getPrice()).isEqualTo(20000L);
            assertThat(product.getDescription()).isEqualTo("새 설명");
        }

        @Test
        @DisplayName("판매 종료된 상품은 정보를 수정할 수 없다")
        void modifyProduct_whenDiscontinued_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.DISCONTINUED);

            // when & then
            assertThatThrownBy(() -> product.modify("새 상품명", 20000L, "KRW", "새 설명"))
                    .isInstanceOf(InvalidProductStatusException.class);
        }
    }

    @Nested
    @DisplayName("상품 상태 변경")
    class ChangeStatus {

        @Test
        @DisplayName("DRAFT 상품을 판매 활성화할 수 있다")
        void changeStatus_fromDraftToActive_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");

            // when
            product.changeStatus(ProductStatus.ACTIVE);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("DRAFT 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromDraftToDiscontinued_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");

            // when
            product.changeStatus(ProductStatus.DISCONTINUED);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("DRAFT 상품을 INACTIVE로 바로 전이할 수 없다")
        void changeStatus_fromDraftToInactive_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");

            // when & then
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.INACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 중인 상품을 판매 중단할 수 있다")
        void changeStatus_fromActiveToInactive_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);

            // when
            product.changeStatus(ProductStatus.INACTIVE);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.INACTIVE);
        }

        @Test
        @DisplayName("판매 중인 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromActiveToDiscontinued_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);

            // when
            product.changeStatus(ProductStatus.DISCONTINUED);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("판매 중인 상품을 DRAFT로 되돌릴 수 없다")
        void changeStatus_fromActiveToDraft_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);

            // when & then
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 중단된 상품을 재판매할 수 있다")
        void changeStatus_fromInactiveToActive_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            // when
            product.changeStatus(ProductStatus.ACTIVE);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        @DisplayName("판매 중단된 상품을 판매 종료 처리할 수 있다")
        void changeStatus_fromInactiveToDiscontinued_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            // when
            product.changeStatus(ProductStatus.DISCONTINUED);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }

        @Test
        @DisplayName("판매 중단된 상품을 DRAFT로 되돌릴 수 없다")
        void changeStatus_fromInactiveToDraft_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.ACTIVE);
            product.changeStatus(ProductStatus.INACTIVE);

            // when & then
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.DRAFT))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료된 상품은 다시 판매 활성화할 수 없다")
        void changeStatus_fromDiscontinuedToActive_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.DISCONTINUED);

            // when & then
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.ACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료된 상품은 비활성으로도 되돌릴 수 없다")
        void changeStatus_fromDiscontinuedToInactive_throwsException() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.DISCONTINUED);

            // when & then
            assertThatThrownBy(() -> product.changeStatus(ProductStatus.INACTIVE))
                    .isInstanceOf(InvalidProductStatusException.class);
        }

        @Test
        @DisplayName("판매 종료 상태를 판매 종료로 다시 설정해도 예외가 발생하지 않는다")
        void changeStatus_fromDiscontinuedToDiscontinued_succeeds() {
            // given
            Product product = Product.create("상품명", 10000L, "KRW", "설명");
            product.changeStatus(ProductStatus.DISCONTINUED);

            // when
            product.changeStatus(ProductStatus.DISCONTINUED);

            // then
            assertThat(product.getProductStatus()).isEqualTo(ProductStatus.DISCONTINUED);
        }
    }
}
