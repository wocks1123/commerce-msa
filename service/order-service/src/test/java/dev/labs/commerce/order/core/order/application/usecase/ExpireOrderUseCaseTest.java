package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderExpiredEvent;
import dev.labs.commerce.order.core.order.application.usecase.dto.ExpireOrderCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
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
class ExpireOrderUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private ExpireOrderUseCase expireOrderUseCase;

    @Test
    @DisplayName("CREATED 주문을 EXPIRED로 전이하고 OrderExpiredEvent를 발행한다")
    void execute_fromCreated_expiresAndPublishesEvent() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.CREATED);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        expireOrderUseCase.execute(new ExpireOrderCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        final ArgumentCaptor<OrderExpiredEvent> captor = ArgumentCaptor.forClass(OrderExpiredEvent.class);
        then(orderEventPublisher).should().publishOrderExpired(captor.capture());
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().items()).isNotEmpty();
    }

    @Test
    @DisplayName("PENDING 주문도 EXPIRED로 전이할 수 있다")
    void execute_fromPending_expiresSuccessfully() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.PENDING);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        expireOrderUseCase.execute(new ExpireOrderCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.EXPIRED);
        then(orderEventPublisher).should().publishOrderExpired(any(OrderExpiredEvent.class));
    }

    @Test
    @DisplayName("CREATED/PENDING이 아닌 상태이면 아무 동작도 하지 않는다 (멱등)")
    void execute_whenNotCreatedOrPending_doesNothing() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.PAID);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        expireOrderUseCase.execute(new ExpireOrderCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
        then(orderEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("주문을 찾지 못하면 OrderNotFoundException이 발생한다")
    void execute_whenOrderNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> expireOrderUseCase.execute(new ExpireOrderCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);
        then(orderEventPublisher).should(never()).publishOrderExpired(any());
    }

    private SalesOrder orderWithStatus(String orderId, OrderStatus status) {
        return SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(status)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
    }
}
