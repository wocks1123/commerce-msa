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
class GetPaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private GetPaymentUseCase getPaymentUseCase;

    @Test
    @DisplayName("paymentId로 결제 정보를 조회하면 결제 상세를 반환한다")
    void execute_returnsPaymentDetail() {
        // given
        final String paymentId = "p-1";
        final Payment payment = PaymentFixture.builder()
                .withSample()
                .paymentId(paymentId)
                .status(PaymentStatus.APPROVED)
                .build();
        given(paymentRepository.findById(paymentId)).willReturn(Optional.of(payment));

        // when
        final GetPaymentResult actual = getPaymentUseCase.execute(paymentId);

        // then
        assertThat(actual.paymentId()).isEqualTo(paymentId);
        assertThat(actual.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(actual.amount()).isEqualTo(payment.getAmount());
        assertThat(actual.orderId()).isEqualTo(payment.getOrderId());
    }

    @Test
    @DisplayName("결제를 찾지 못하면 PaymentNotFoundException이 발생한다")
    void execute_whenPaymentNotFound_throwsException() {
        // given
        final String paymentId = "missing";
        given(paymentRepository.findById(paymentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getPaymentUseCase.execute(paymentId))
                .isInstanceOf(PaymentNotFoundException.class);
    }
}
