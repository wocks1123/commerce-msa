package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.application.event.PaymentApprovedEvent;
import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FinalizePaymentCommandServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private FinalizePaymentCommandService finalizePaymentCommandService;

    @Test
    @DisplayName("PG 승인 성공 + 금액 일치하면 APPROVED 상태로 전이하고 PaymentApprovedEvent를 발행한다")
    void finalize_whenSuccessAndAmountMatch_approvesAndPublishes() {
        // given
        final String orderId = "order-1";
        final long amount = 10000L;
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .amount(amount)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final PgApprovalResult pgResult = PgApprovalResult.success("pg-tx-1", amount, Instant.now());

        // when
        final FinalizePaymentResult actual = finalizePaymentCommandService.finalize(orderId, pgResult);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        final ArgumentCaptor<PaymentApprovedEvent> captor = ArgumentCaptor.forClass(PaymentApprovedEvent.class);
        then(eventPublisher).should().publishPaymentApproved(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        then(eventPublisher).should(never()).publishPaymentFailed(any());
    }

    @Test
    @DisplayName("PG 승인 성공이지만 금액이 일치하지 않으면 AMOUNT_MISMATCH로 FAILED 처리되고 PaymentFailedEvent를 발행한다")
    void finalize_whenAmountMismatch_failsWithMismatchCode() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .amount(10000L)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final PgApprovalResult pgResult = PgApprovalResult.success("pg-tx-1", 9999L, Instant.now());

        // when
        final FinalizePaymentResult actual = finalizePaymentCommandService.finalize(orderId, pgResult);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo("AMOUNT_MISMATCH");
        final ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        then(eventPublisher).should().publishPaymentFailed(captor.capture());
        assertThat(captor.getValue().failureCode()).isEqualTo("AMOUNT_MISMATCH");
        then(eventPublisher).should(never()).publishPaymentApproved(any());
    }

    @Test
    @DisplayName("PG 거절(failure)이면 FAILED 상태로 전이하고 PaymentFailedEvent를 발행한다")
    void finalize_whenFailure_failsAndPublishesEvent() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .amount(10000L)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final PgApprovalResult pgResult = PgApprovalResult.failure("CARD_DECLINED", "한도 초과");

        // when
        final FinalizePaymentResult actual = finalizePaymentCommandService.finalize(orderId, pgResult);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo("CARD_DECLINED");
        assertThat(payment.getFailureMessage()).isEqualTo("한도 초과");
        then(eventPublisher).should().publishPaymentFailed(any(PaymentFailedEvent.class));
        then(eventPublisher).should(never()).publishPaymentApproved(any());
    }

    @Test
    @DisplayName("PG aborted이면 ABORTED 상태로 전이하고 이벤트는 발행되지 않는다")
    void finalize_whenAborted_abortsWithoutEvent() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .amount(10000L)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final PgApprovalResult pgResult = PgApprovalResult.ofAborted("PG_TIMEOUT", "PG 응답 지연");

        // when
        final FinalizePaymentResult actual = finalizePaymentCommandService.finalize(orderId, pgResult);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.ABORTED);
        assertThat(payment.getFailureCode()).isEqualTo("PG_TIMEOUT");
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void finalize_whenPaymentNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.empty());
        final PgApprovalResult pgResult = PgApprovalResult.success("pg-tx-1", 10000L, Instant.now());

        // when & then
        assertThatThrownBy(() -> finalizePaymentCommandService.finalize(orderId, pgResult))
                .isInstanceOf(PaymentNotFoundException.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
        then(eventPublisher).shouldHaveNoInteractions();
    }
}
