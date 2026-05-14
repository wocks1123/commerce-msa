package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.GetPaymentResult;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
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
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetPaymentByOrderUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentByOrderUseCase getPaymentByOrderUseCase;

    @Test
    @DisplayName("orderId로 결제 정보를 조회하면 결제 상세를 반환한다")
    void execute_returnsPaymentDetail() {
        // given
        final String orderId = "o-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(PaymentStatus.IN_PROGRESS)
                .build();
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.of(payment));

        // when
        final GetPaymentResult actual = getPaymentByOrderUseCase.execute(orderId);

        // then
        assertThat(actual.orderId()).isEqualTo(orderId);
        assertThat(actual.status()).isEqualTo(PaymentStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void execute_whenPaymentNotFound_throwsException() {
        // given
        final String orderId = "missing-order";
        given(paymentRepository.findByOrderId(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getPaymentByOrderUseCase.execute(orderId))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
