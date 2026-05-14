package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentInitializedEvent;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.InventoryPort;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import dev.labs.commerce.payment.core.payment.domain.fixture.PaymentFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class InitializePaymentUseCaseTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InventoryPort inventoryPort;

    @Mock
    private PaymentEventPublisher paymentEventPublisher;

    @InjectMocks
    private InitializePaymentUseCase initializePaymentUseCase;

    @Test
    @DisplayName("결제를 초기화하면 재고를 예약하고 결제를 저장하며 PaymentInitializedEvent를 발행한다")
    void execute_initializesPayment() {
        // given
        final InitializePaymentCommand command = sampleCommand("order-1", "idem-1");
        final Payment saved = PaymentFixture.builder()
                .withSample()
                .orderId(command.orderId())
                .status(PaymentStatus.REQUESTED)
                .amount(command.amount())
                .currency(command.currency())
                .requestedAt(command.requestedAt())
                .build();
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(false);
        given(paymentRepository.findByIdempotencyKey(command.idempotencyKey())).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willReturn(saved);

        // when
        final InitializePaymentResult actual = initializePaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.REQUESTED);
        assertThat(actual.orderId()).isEqualTo(command.orderId());
        then(inventoryPort).should().reserve(eq(command.orderId()), anyList());
        final ArgumentCaptor<PaymentInitializedEvent> captor = ArgumentCaptor.forClass(PaymentInitializedEvent.class);
        then(paymentEventPublisher).should().publishPaymentInitialized(captor.capture());
        assertThat(captor.getValue().paymentId()).isEqualTo(saved.getPaymentId());
        assertThat(captor.getValue().orderId()).isEqualTo(command.orderId());
    }

    @Test
    @DisplayName("Command의 items가 InventoryPort.Item으로 변환되어 reserve에 전달된다")
    void execute_passesItemsToInventoryPort() {
        // given
        final InitializePaymentCommand command = sampleCommand("order-2", "idem-2");
        final Payment saved = PaymentFixture.builder().withSample().orderId(command.orderId()).build();
        given(paymentRepository.existsByOrderId(anyString())).willReturn(false);
        given(paymentRepository.findByIdempotencyKey(anyString())).willReturn(Optional.empty());
        given(paymentRepository.save(any(Payment.class))).willReturn(saved);

        // when
        initializePaymentUseCase.execute(command);

        // then
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<List<InventoryPort.Item>> itemsCaptor = ArgumentCaptor.forClass(List.class);
        then(inventoryPort).should().reserve(eq(command.orderId()), itemsCaptor.capture());
        assertThat(itemsCaptor.getValue())
                .extracting(InventoryPort.Item::productId, InventoryPort.Item::quantity)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(10L, 2),
                        org.assertj.core.groups.Tuple.tuple(20L, 1)
                );
    }

    @Test
    @DisplayName("동일 orderId의 결제가 이미 존재하면 PaymentAlreadyExistsException이 발생한다")
    void execute_whenOrderIdExists_throwsException() {
        // given
        final InitializePaymentCommand command = sampleCommand("order-dup", "idem-1");
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> initializePaymentUseCase.execute(command))
                .isInstanceOf(PaymentAlreadyExistsException.class);
        then(inventoryPort).shouldHaveNoInteractions();
        then(paymentRepository).should(never()).save(any(Payment.class));
        then(paymentEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("동일 idempotencyKey의 결제가 이미 존재하면 PaymentAlreadyExistsException이 발생한다")
    void execute_whenIdempotencyKeyExists_throwsException() {
        // given
        final InitializePaymentCommand command = sampleCommand("order-1", "idem-dup");
        final Payment existing = PaymentFixture.builder().withSample().build();
        given(paymentRepository.existsByOrderId(command.orderId())).willReturn(false);
        given(paymentRepository.findByIdempotencyKey(command.idempotencyKey())).willReturn(Optional.of(existing));

        // when & then
        assertThatThrownBy(() -> initializePaymentUseCase.execute(command))
                .isInstanceOf(PaymentAlreadyExistsException.class);
        then(inventoryPort).shouldHaveNoInteractions();
        then(paymentRepository).should(never()).save(any(Payment.class));
        then(paymentEventPublisher).shouldHaveNoInteractions();
    }

    private InitializePaymentCommand sampleCommand(String orderId, String idempotencyKey) {
        return new InitializePaymentCommand(
                orderId,
                100L,
                10000L,
                "KRW",
                idempotencyKey,
                PgProvider.MOCK_PAY,
                Instant.now(),
                List.of(
                        new InitializePaymentCommand.Item(10L, 2),
                        new InitializePaymentCommand.Item(20L, 1)
                )
        );
    }
}
