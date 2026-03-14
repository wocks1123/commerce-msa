package dev.labs.commerce.order.core.order.domain;

import dev.labs.commerce.order.core.order.domain.error.InvalidOrderStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesOrderTest {

    private OrderItem sampleItem() {
        return OrderItem.create(1L, "상품A", 5000L, 2, "KRW");
    }

    private SalesOrder pendingOrder() {
        return SalesOrder.create(100L, "KRW", List.of(sampleItem()), Instant.now());
    }

    @Nested
    @DisplayName("주문 생성")
    class Create {

        @Test
        @DisplayName("유효한 정보로 주문을 생성하면 주문 접수 상태로 생성된다")
        void createOrder_withValidInfo_startsAsPending() {
            // given
            long customerId = 100L;
            List<OrderItem> items = List.of(sampleItem());

            // when
            SalesOrder order = SalesOrder.create(customerId, "KRW", items, Instant.now());

            // then
            assertThat(order.getOrderId()).isNotBlank();
            assertThat(order.getCustomerId()).isEqualTo(customerId);
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(order.getCurrency()).isEqualTo("KRW");
            assertThat(order.getPendingAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 생성 시 총 결제 금액은 주문 항목 금액의 합산이다")
        void createOrder_totalPriceEqualsSumOfLineAmounts() {
            // given
            OrderItem item1 = OrderItem.create(1L, "상품A", 5000L, 2, "KRW"); // 10,000
            OrderItem item2 = OrderItem.create(2L, "상품B", 3000L, 3, "KRW"); // 9,000

            // when
            SalesOrder order = SalesOrder.create(100L, "KRW", List.of(item1, item2), Instant.now());

            // then
            assertThat(order.getTotalPrice()).isEqualTo(19000L);
            assertThat(order.getTotalAmount()).isEqualTo(5L);
        }

        @Test
        @DisplayName("주문 항목이 없으면 주문을 생성할 수 없다")
        void createOrder_withEmptyItems_throwsException() {
            // when & then
            assertThatThrownBy(() -> SalesOrder.create(100L, "KRW", List.of(), Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("통화 단위가 없으면 주문을 생성할 수 없다")
        void createOrder_withBlankCurrency_throwsException() {
            // given
            List<OrderItem> items = List.of(sampleItem());

            // when & then
            assertThatThrownBy(() -> SalesOrder.create(100L, " ", items, Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("주문 중단")
    class Abort {

        @Test
        @DisplayName("주문 접수 상태에서 중단하면 중단 상태로 전환된다")
        void abort_whenPending_transitionsToAborted() {
            // given
            SalesOrder order = pendingOrder();

            // when
            order.abort(Instant.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.ABORTED);
            assertThat(order.getAbortedAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 접수 상태가 아닌 주문은 중단 처리를 할 수 없다")
        void abort_whenNotPending_throwsException() {
            // given
            SalesOrder order = pendingOrder();
            order.abort(Instant.now());

            // when & then
            assertThatThrownBy(() -> order.abort(Instant.now()))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("결제 완료")
    class ConfirmPaid {

        @Test
        @DisplayName("주문 접수 상태에서 결제가 완료되면 결제 완료 상태로 전환된다")
        void confirmPaid_whenPending_transitionsToPaid() {
            // given
            SalesOrder order = pendingOrder();

            // when
            order.confirmPaid(Instant.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
            assertThat(order.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 접수 상태가 아닌 주문은 결제 완료 처리를 할 수 없다")
        void confirmPaid_whenNotPending_throwsException() {
            // given
            SalesOrder order = pendingOrder();
            order.abort(Instant.now());

            // when & then
            assertThatThrownBy(() -> order.confirmPaid(Instant.now()))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 취소")
    class Cancel {

        @Test
        @DisplayName("주문 접수 상태에서 취소하면 취소 상태로 전환된다")
        void cancel_whenPending_transitionsToCancelled() {
            // given
            SalesOrder order = pendingOrder();

            // when
            order.cancel(Instant.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
            assertThat(order.getCancelledAt()).isNotNull();
        }

        @Test
        @DisplayName("주문 접수 상태가 아닌 주문은 취소할 수 없다")
        void cancel_whenNotPending_throwsException() {
            // given
            SalesOrder order = pendingOrder();
            order.abort(Instant.now());

            // when & then
            assertThatThrownBy(() -> order.cancel(Instant.now()))
                    .isInstanceOf(InvalidOrderStateException.class);
        }
    }

    @Nested
    @DisplayName("주문 만료")
    class MarkAsExpired {

        @Test
        @DisplayName("주문 접수 상태에서 만료되면 만료 상태로 전환된다")
        void markAsExpired_whenPending_transitionsToExpired() {
            // given
            SalesOrder order = pendingOrder();

            // when
            order.markAsExpired(Instant.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
            assertThat(order.getExpiredAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("시스템 오류 처리")
    class MarkAsFailed {

        @Test
        @DisplayName("어떤 상태의 주문이든 시스템 오류로 실패 처리할 수 있다")
        void markAsFailed_fromAnyStatus_transitionsToFailed() {
            // given
            SalesOrder order = pendingOrder();

            // when
            order.markAsFailed(Instant.now());

            // then
            assertThat(order.getStatus()).isEqualTo(OrderStatus.FAILED);
            assertThat(order.getFailedAt()).isNotNull();
        }
    }
}
