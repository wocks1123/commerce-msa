package dev.labs.commerce.payment.core.payment.domain;

import dev.labs.commerce.payment.core.payment.domain.exception.PaymentInvalidStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PaymentTest {

    private Payment requestedPayment() {
        return Payment.create("order-1", 100L, 10000L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, Instant.now());
    }

    private Payment approvedPayment() {
        return Payment.createApproved("order-1", 100L, 10000L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, "pg-tx-1", Instant.now());
    }

    @Nested
    @DisplayName("결제 요청 생성")
    class Create {

        @Test
        @DisplayName("유효한 정보로 결제를 요청하면 결제 요청 상태로 생성된다")
        void createPayment_withValidInfo_startsAsRequested() {
            // given
            String orderId = "order-1";
            long customerId = 100L;
            long amount = 10000L;

            // when
            Payment payment = Payment.create(orderId, customerId, amount, "KRW", "idem-key-1", PgProvider.MOCK_PAY, Instant.now());

            // then
            assertThat(payment.getPaymentId()).isNotBlank();
            assertThat(payment.getOrderId()).isEqualTo(orderId);
            assertThat(payment.getCustomerId()).isEqualTo(customerId);
            assertThat(payment.getAmount()).isEqualTo(amount);
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
        }

        @Test
        @DisplayName("주문 ID가 없으면 결제를 요청할 수 없다")
        void createPayment_withBlankOrderId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Payment.create(" ", 100L, 10000L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("결제 금액이 음수이면 결제를 요청할 수 없다")
        void createPayment_withNegativeAmount_throwsException() {
            // when & then
            assertThatThrownBy(() -> Payment.create("order-1", 100L, -1L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("멱등키가 없으면 결제를 요청할 수 없다")
        void createPayment_withBlankIdempotencyKey_throwsException() {
            // when & then
            assertThatThrownBy(() -> Payment.create("order-1", 100L, 10000L, "KRW", " ", PgProvider.MOCK_PAY, Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("즉시 승인 결제 생성")
    class CreateApproved {

        @Test
        @DisplayName("PG 거래 ID와 함께 즉시 승인 결제를 생성하면 승인 상태로 생성된다")
        void createApprovedPayment_withPgTxId_startsAsApproved() {
            // given
            String pgTxId = "pg-tx-1";

            // when
            Payment payment = Payment.createApproved("order-1", 100L, 10000L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, pgTxId, Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getPgTxId()).isEqualTo(pgTxId);
            assertThat(payment.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("PG 거래 ID가 없으면 즉시 승인 결제를 생성할 수 없다")
        void createApprovedPayment_withBlankPgTxId_throwsException() {
            // when & then
            assertThatThrownBy(() -> Payment.createApproved("order-1", 100L, 10000L, "KRW", "idem-key-1", PgProvider.MOCK_PAY, " ", Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class Approve {

        @Test
        @DisplayName("결제 요청 상태에서 PG 승인이 완료되면 승인 상태로 전환된다")
        void approve_whenRequested_transitionsToApproved() {
            // given
            Payment payment = requestedPayment();

            // when
            payment.approve("pg-tx-1", Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getPgTxId()).isEqualTo("pg-tx-1");
            assertThat(payment.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 요청 상태가 아닌 결제는 승인 처리를 할 수 없다")
        void approve_whenNotRequested_throwsException() {
            // given
            Payment payment = approvedPayment();

            // when & then
            assertThatThrownBy(() -> payment.approve("pg-tx-2", Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }

        @Test
        @DisplayName("PG 거래 ID 없이 결제 승인을 처리할 수 없다")
        void approve_withBlankPgTxId_throwsException() {
            // given
            Payment payment = requestedPayment();

            // when & then
            assertThatThrownBy(() -> payment.approve(" ", Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class Fail {

        @Test
        @DisplayName("결제 요청 상태에서 PG 거절이 발생하면 실패 상태로 전환된다")
        void fail_whenRequested_transitionsToFailed() {
            // given
            Payment payment = requestedPayment();

            // when
            payment.fail("CARD_DECLINED", "한도 초과", Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("CARD_DECLINED");
            assertThat(payment.getFailureMessage()).isEqualTo("한도 초과");
            assertThat(payment.getFailedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 요청 상태가 아닌 결제는 실패 처리를 할 수 없다")
        void fail_whenNotRequested_throwsException() {
            // given
            Payment payment = approvedPayment();

            // when & then
            assertThatThrownBy(() -> payment.fail("CARD_DECLINED", null, Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }
    }

    @Nested
    @DisplayName("결제 취소")
    class Cancel {

        @Test
        @DisplayName("승인된 결제를 취소하면 취소 상태로 전환된다")
        void cancel_whenApproved_transitionsToCanceled() {
            // given
            Payment payment = approvedPayment();

            // when
            payment.cancel(Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CANCELED);
            assertThat(payment.getCanceledAt()).isNotNull();
        }

        @Test
        @DisplayName("승인 상태가 아닌 결제는 취소할 수 없다")
        void cancel_whenNotApproved_throwsException() {
            // given
            Payment payment = requestedPayment();

            // when & then
            assertThatThrownBy(() -> payment.cancel(Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }
    }
}
