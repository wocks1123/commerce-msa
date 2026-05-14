package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.RegisterApprovedPaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.RegisterApprovedPaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import dev.labs.commerce.payment.core.payment.domain.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RegisterApprovedPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private RegisterApprovedPaymentUseCase registerApprovedPaymentUseCase;

    @Test
    @DisplayName("승인된 결제를 등록하면 APPROVED 상태로 저장된 결과를 반환한다")
    void execute_registersApprovedPayment() {
        // given
        final RegisterApprovedPaymentCommand command = sampleCommand("order-1", "idem-1");
        final Payment saved = PaymentFixture.builder()
                .withSample()
                .orderId(command.orderId())
                .status(PaymentStatus.APPROVED)
                .amount(command.amount())
                .currency(command.currency())
                .pgTxId(command.pgTxId())
                .approvedAt(command.approvedAt())
                .build();
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(false);
        given(paymentRepository.findByIdempotencyKey(command.idempotencyKey())).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willReturn(saved);

        // when
        final RegisterApprovedPaymentResult actual = registerApprovedPaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(actual.orderId()).isEqualTo(command.orderId());
        assertThat(actual.pgTxId()).isEqualTo(command.pgTxId());
        then(paymentRepository).should().save(any(Payment.class));
    }

    @Test
    @DisplayName("동일 orderId의 결제가 이미 존재하면 PaymentAlreadyExistsException이 발생한다")
    void execute_whenOrderIdExists_throwsException() {
        // given
        final RegisterApprovedPaymentCommand command = sampleCommand("order-dup", "idem-1");
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> registerApprovedPaymentUseCase.execute(command))
                .isInstanceOf(PaymentAlreadyExistsException.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    @Test
    @DisplayName("동일 idempotencyKey의 결제가 이미 존재하면 PaymentAlreadyExistsException이 발생한다")
    void execute_whenIdempotencyKeyExists_throwsException() {
        // given
        final RegisterApprovedPaymentCommand command = sampleCommand("order-1", "idem-dup");
        final Payment existing = PaymentFixture.builder().withSample().build();
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(false);
        given(paymentRepository.findByIdempotencyKey(command.idempotencyKey())).willReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> registerApprovedPaymentUseCase.execute(command))
                .isInstanceOf(PaymentAlreadyExistsException.class);
        then(paymentRepository).should(never()).save(any(Payment.class));
    }

    private RegisterApprovedPaymentCommand sampleCommand(String orderId, String idempotencyKey) {
        return new RegisterApprovedPaymentCommand(
                orderId,
                100L,
                10000L,
                "KRW",
                idempotencyKey,
                PgProvider.MOCK_PAY,
                "pg-tx-1",
                Instant.now()
        );
    }
}
