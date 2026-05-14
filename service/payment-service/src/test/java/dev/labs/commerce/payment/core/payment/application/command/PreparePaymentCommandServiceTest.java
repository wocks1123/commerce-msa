package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAmountMismatchException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentNotFoundException;
import dev.labs.commerce.payment.core.payment.domain.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class PreparePaymentCommandServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PreparePaymentCommandService preparePaymentCommandService;

    @Test
    @DisplayName("REQUESTED 상태 + 금액 일치하면 IN_PROGRESS로 전이하고 ProceedPaymentResult를 반환한다")
    void prepare_whenRequestedAndAmountMatches_returnsProceed() {
        // given
        final String orderId = "order-1";
        final long amount = 10000L;
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.REQUESTED)
                .amount(amount)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        given(paymentRepository.save(payment)).willReturn(payment);
        final ApprovePaymentCommand command = new ApprovePaymentCommand(orderId, "pk-1", amount);

        // when
        final PreparePaymentResult actual = preparePaymentCommandService.prepare(command);

        // then
        assertThat(actual).isInstanceOf(ProceedPaymentResult.class);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.IN_PROGRESS);
        then(paymentRepository).should().save(payment);
    }

    @Test
    @DisplayName("이미 IN_PROGRESS 등 REQUESTED가 아닌 상태이면 SkipPaymentResult를 반환한다")
    void prepare_whenAlreadyInProgress_returnsSkip() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        final ApprovePaymentCommand command = new ApprovePaymentCommand(orderId, "pk-1", 10000L);

        // when
        final PreparePaymentResult actual = preparePaymentCommandService.prepare(command);

        // then
        assertThat(actual).isInstanceOf(SkipPaymentResult.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("REQUESTED 상태인데 결제 금액이 일치하지 않으면 PaymentAmountMismatchException이 발생한다")
    void prepare_whenAmountMismatch_throwsException() {
        // given
        final String orderId = "order-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.REQUESTED)
                .amount(10000L)
                .build();
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.of(payment));
        final ApprovePaymentCommand command = new ApprovePaymentCommand(orderId, "pk-1", 9999L);

        // when & then
        assertThatThrownBy(() -> preparePaymentCommandService.prepare(command))
                .isInstanceOf(PaymentAmountMismatchException.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REQUESTED);
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void prepare_whenPaymentNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(paymentRepository.findByOrderIdWithLock(orderId)).willReturn(Optional.empty());
        final ApprovePaymentCommand command = new ApprovePaymentCommand(orderId, "pk-1", 10000L);

        // when & then
        assertThatThrownBy(() -> preparePaymentCommandService.prepare(command))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
