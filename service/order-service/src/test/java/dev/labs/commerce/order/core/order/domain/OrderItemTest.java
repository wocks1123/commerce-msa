package dev.labs.commerce.order.core.order.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderItemTest {

    @Nested
    @DisplayName("주문 항목 생성")
    class Create {

        @Test
        @DisplayName("유효한 정보로 주문 항목을 생성하면 금액은 단가 × 수량으로 계산된다")
        void createOrderItem_lineAmountEqualsUnitPriceTimesQuantity() {
            // given
            long unitPrice = 3000L;
            int quantity = 4;

            // when
            OrderItem item = OrderItem.create(1L, "상품A", unitPrice, quantity, "KRW");

            // then
            assertThat(item.getLineAmount()).isEqualTo(12000L);
            assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
            assertThat(item.getQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("상품명이 없으면 주문 항목을 생성할 수 없다")
        void createOrderItem_withBlankProductName_throwsException() {
            // when & then
            assertThatThrownBy(() -> OrderItem.create(1L, " ", 3000L, 1, "KRW"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("단가가 0 이하이면 주문 항목을 생성할 수 없다")
        void createOrderItem_withZeroOrNegativeUnitPrice_throwsException() {
            // when & then
            assertThatThrownBy(() -> OrderItem.create(1L, "상품A", 0L, 1, "KRW"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("수량이 0 이하이면 주문 항목을 생성할 수 없다")
        void createOrderItem_withZeroOrNegativeQuantity_throwsException() {
            // when & then
            assertThatThrownBy(() -> OrderItem.create(1L, "상품A", 3000L, 0, "KRW"))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("통화 단위가 없으면 주문 항목을 생성할 수 없다")
        void createOrderItem_withBlankCurrency_throwsException() {
            // when & then
            assertThatThrownBy(() -> OrderItem.create(1L, "상품A", 3000L, 1, " "))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
