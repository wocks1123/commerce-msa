package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentFailedEvent;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentResult;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FailPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher eventPublisher;

    @InjectMocks
    private FailPaymentUseCase failPaymentUseCase;

    @Test
    @DisplayName("REQUESTED 상태의 결제를 실패 처리하면 FAILED 상태로 전이하고 PaymentFailedEvent를 발행한다")
    void execute_whenRequested_failsAndPublishesEvent() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.REQUESTED)
                .build();
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final FailPaymentCommand command = new FailPaymentCommand(orderId, "INSUFFICIENT_STOCK", "재고 부족");

        // when
        final FailPaymentResult actual = failPaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(actual.failureCode()).isEqualTo("INSUFFICIENT_STOCK");
        final ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        then(eventPublisher).should().publishPaymentFailed(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().failureCode()).isEqualTo("INSUFFICIENT_STOCK");
    }

    @Test
    @DisplayName("REQUESTED가 아닌 상태이면 현재 상태를 그대로 반환하고 이벤트는 발행되지 않는다")
    void execute_whenNotRequested_returnsCurrentStateWithoutEvent() {
        // given - 이미 IN_PROGRESS 상태
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .build();
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));
        final FailPaymentCommand command = new FailPaymentCommand(orderId, "EXTERNAL_FAILURE", null);

        // when
        final FailPaymentResult actual = failPaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.IN_PROGRESS);
        then(paymentRepository).should(never()).save(any(Payment.class));
        then(eventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void execute_whenPaymentNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());
        final FailPaymentCommand command = new FailPaymentCommand(orderId, "EXTERNAL_FAILURE", null);

        // when & then
        assertThatThrownBy(() -> failPaymentUseCase.execute(command))
                .isInstanceOf(PaymentNotFoundException.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
        then(eventPublisher).shouldHaveNoInteractions();
    }
}
