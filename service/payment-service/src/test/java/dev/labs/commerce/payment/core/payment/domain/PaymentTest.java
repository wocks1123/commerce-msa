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

    private Payment inProgressPayment() {
        Payment payment = requestedPayment();
        payment.markInProgress(Instant.now());
        return payment;
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
    @DisplayName("PG 승인 진행 중")
    class MarkInProgress {

        @Test
        @DisplayName("결제 요청 상태에서 PG 호출을 시작하면 진행 중 상태로 전환된다")
        void markInProgress_whenRequested_transitionsToInProgress() {
            // given
            Payment payment = requestedPayment();
            Instant inProgressAt = Instant.now();

            // when
            payment.markInProgress(inProgressAt);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
            assertThat(payment.getInProgressAt()).isEqualTo(inProgressAt);
        }

        @Test
        @DisplayName("결제 요청 상태가 아닌 결제는 진행 중으로 전환할 수 없다")
        void markInProgress_whenNotRequested_throwsException() {
            // given
            Payment payment = approvedPayment();

            // when & then
            assertThatThrownBy(() -> payment.markInProgress(Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }

        @Test
        @DisplayName("시각 없이 진행 중으로 전환할 수 없다")
        void markInProgress_withNullInstant_throwsException() {
            // given
            Payment payment = requestedPayment();

            // when & then
            assertThatThrownBy(() -> payment.markInProgress(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 승인")
    class Approve {

        @Test
        @DisplayName("PG 진행 중 상태에서 PG 승인이 완료되면 승인 상태로 전환된다")
        void approve_whenInProgress_transitionsToApproved() {
            // given
            Payment payment = inProgressPayment();

            // when
            payment.approve("pg-tx-1", Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
            assertThat(payment.getPgTxId()).isEqualTo("pg-tx-1");
            assertThat(payment.getApprovedAt()).isNotNull();
        }

        @Test
        @DisplayName("PG 진행 중 상태가 아닌 결제는 승인 처리를 할 수 없다")
        void approve_whenNotInProgress_throwsException() {
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
            Payment payment = inProgressPayment();

            // when & then
            assertThatThrownBy(() -> payment.approve(" ", Instant.now()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("결제 실패")
    class Fail {

        @Test
        @DisplayName("PG 진행 중 상태에서 PG 거절이 발생하면 실패 상태로 전환된다")
        void fail_whenInProgress_transitionsToFailed() {
            // given
            Payment payment = inProgressPayment();

            // when
            payment.fail("CARD_DECLINED", "한도 초과", Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("CARD_DECLINED");
            assertThat(payment.getFailureMessage()).isEqualTo("한도 초과");
            assertThat(payment.getFailedAt()).isNotNull();
        }

        @Test
        @DisplayName("결제 요청 상태에서도 실패 처리를 할 수 있다")
        void fail_whenRequested_transitionsToFailed() {
            // given
            Payment payment = requestedPayment();

            // when
            payment.fail("PG_REJECTED", "PG 직접 거절", Instant.now());

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
            assertThat(payment.getFailureCode()).isEqualTo("PG_REJECTED");
            assertThat(payment.getFailedAt()).isNotNull();
        }

        @Test
        @DisplayName("REQUESTED, IN_PROGRESS 외 상태의 결제는 실패 처리를 할 수 없다")
        void fail_whenApproved_throwsException() {
            // given
            Payment payment = approvedPayment();

            // when & then
            assertThatThrownBy(() -> payment.fail("CARD_DECLINED", null, Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }
    }

    @Nested
    @DisplayName("결제 중단 (PG 호출 예외)")
    class Abort {

        @Test
        @DisplayName("PG 진행 중 상태에서 PG 호출 예외가 발생하면 중단 상태로 전환된다")
        void abort_whenInProgress_transitionsToAborted() {
            // given
            Payment payment = inProgressPayment();
            Instant abortedAt = Instant.now();

            // when
            payment.abort(abortedAt);

            // then
            assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
            assertThat(payment.getAbortedAt()).isEqualTo(abortedAt);
        }

        @Test
        @DisplayName("PG 진행 중 상태가 아닌 결제는 중단 처리를 할 수 없다")
        void abort_whenNotInProgress_throwsException() {
            // given
            Payment payment = requestedPayment();

            // when & then
            assertThatThrownBy(() -> payment.abort(Instant.now()))
                    .isInstanceOf(PaymentInvalidStatusException.class);
        }

        @Test
        @DisplayName("시각 없이 중단 처리를 할 수 없다")
        void abort_withNullInstant_throwsException() {
            // given
            Payment payment = inProgressPayment();

            // when & then
            assertThatThrownBy(() -> payment.abort(null))
                    .isInstanceOf(IllegalArgumentException.class);
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
