package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaidCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import dev.labs.commerce.order.core.order.domain.error.OrderPaymentMismatchException;
import dev.labs.commerce.order.core.order.domain.fixture.OrderItemFixture;
import dev.labs.commerce.order.core.order.domain.fixture.SalesOrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ConfirmPaidUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private ConfirmPaidUseCase confirmPaidUseCase;

    private static final long CUSTOMER_ID = 100L;
    private static final long TOTAL_PRICE = 10000L;
    private static final String CURRENCY = "KRW";

    @Test
    @DisplayName("PENDING 주문을 PAID로 전이하고 OrderPaidEvent를 발행한다")
    void execute_fromPending_marksPaidAndPublishes() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = pendingOrder(orderId);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID, TOTAL_PRICE, CURRENCY);

        // when
        confirmPaidUseCase.execute(command);

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        final ArgumentCaptor<OrderPaidEvent> captor = ArgumentCaptor.forClass(OrderPaidEvent.class);
        then(orderEventPublisher).should().publishOrderPaid(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().items()).isNotEmpty();
    }

    @Test
    @DisplayName("PENDING이 아닌 상태이면 아무 동작도 하지 않는다 (멱등)")
    void execute_whenNotPending_doesNothing() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(OrderStatus.PAID)
                .customerId(CUSTOMER_ID)
                .totalPrice(TOTAL_PRICE)
                .currency(CURRENCY)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID, TOTAL_PRICE, CURRENCY);

        // when
        confirmPaidUseCase.execute(command);

        // then
        then(orderEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("customerId가 일치하지 않으면 OrderPaymentMismatchException이 발생한다")
    void execute_whenCustomerIdMismatch_throwsException() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = pendingOrder(orderId);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID + 1, TOTAL_PRICE, CURRENCY);

        // when & then
        assertThatThrownBy(() -> confirmPaidUseCase.execute(command))
                .isInstanceOf(OrderPaymentMismatchException.class);
        then(orderEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("결제 금액이 일치하지 않으면 OrderPaymentMismatchException이 발생한다")
    void execute_whenAmountMismatch_throwsException() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = pendingOrder(orderId);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID, TOTAL_PRICE - 1, CURRENCY);

        // when & then
        assertThatThrownBy(() -> confirmPaidUseCase.execute(command))
                .isInstanceOf(OrderPaymentMismatchException.class);
        then(orderEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("통화가 일치하지 않으면 OrderPaymentMismatchException이 발생한다")
    void execute_whenCurrencyMismatch_throwsException() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = pendingOrder(orderId);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID, TOTAL_PRICE, "USD");

        // when & then
        assertThatThrownBy(() -> confirmPaidUseCase.execute(command))
                .isInstanceOf(OrderPaymentMismatchException.class);
        then(orderEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("주문을 찾지 못하면 OrderNotFoundException이 발생한다")
    void execute_whenOrderNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());
        final ConfirmPaidCommand command = new ConfirmPaidCommand(orderId, CUSTOMER_ID, TOTAL_PRICE, CURRENCY);

        // when & then
        assertThatThrownBy(() -> confirmPaidUseCase.execute(command))
                .isInstanceOf(OrderNotFoundException.class);
        then(orderEventPublisher).should(never()).publishOrderPaid(any());
    }

    private SalesOrder pendingOrder(String orderId) {
        return SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .customerId(CUSTOMER_ID)
                .totalPrice(TOTAL_PRICE)
                .currency(CURRENCY)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
    }
}
