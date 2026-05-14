package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentExpiredEvent;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ExpirePaymentCommand;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentInvalidStatusException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ExpirePaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private ExpirePaymentUseCase expirePaymentUseCase;

    @Test
    @DisplayName("REQUESTED 결제를 만료 처리하면 FAILED 상태로 전이하고 PaymentExpiredEvent를 발행한다")
    void execute_whenRequested_expiresAndPublishesEvent() {
        // given
        final String paymentId = "p-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .paymentId(paymentId)
                .status(PaymentStatus.REQUESTED)
                .build();
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when
        expirePaymentUseCase.execute(new ExpirePaymentCommand(paymentId));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(payment.getFailureCode()).isEqualTo("PAYMENT_EXPIRED");
        final ArgumentCaptor<PaymentExpiredEvent> captor = ArgumentCaptor.forClass(PaymentExpiredEvent.class);
        then(eventPublisher).should().publishPaymentExpired(captor.capture());
        assertThat(captor.getValue().paymentId()).isEqualTo(paymentId);
        assertThat(captor.getValue().orderId()).isEqualTo(payment.getOrderId());
    }

    @Test
    @DisplayName("IN_PROGRESS 결제도 만료 처리할 수 있다")
    void execute_whenInProgress_expiresSuccessfully() {
        // given
        final String paymentId = "p-2";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .paymentId(paymentId)
                .status(PaymentStatus.IN_PROGRESS)
                .build();
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when
        expirePaymentUseCase.execute(new ExpirePaymentCommand(paymentId));

        // then
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        then(eventPublisher).should().publishPaymentExpired(org.mockito.ArgumentMatchers.any(PaymentExpiredEvent.class));
    }

    @Test
    @DisplayName("이미 종결 상태(APPROVED)인 결제는 만료 처리 시 PaymentInvalidStatusException이 발생한다")
    void execute_whenAlreadyApproved_throwsException() {
        // given
        final String paymentId = "p-3";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .paymentId(paymentId)
                .status(PaymentStatus.APPROVED)
                .build();
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when & then
        assertThatThrownBy(() -> expirePaymentUseCase.execute(new ExpirePaymentCommand(paymentId)))
                .isInstanceOf(PaymentInvalidStatusException.class);
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void execute_whenPaymentNotFound_throwsException() {
        // given
        final String paymentId = "missing";
        given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> expirePaymentUseCase.execute(new ExpirePaymentCommand(paymentId)))
                .isInstanceOf(PaymentNotFoundException.class);
        then(eventPublisher).shouldHaveNoInteractions();
    }
}
