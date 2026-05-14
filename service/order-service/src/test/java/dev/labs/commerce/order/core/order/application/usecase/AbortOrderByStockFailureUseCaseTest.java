package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByStockFailureCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import dev.labs.commerce.order.core.order.domain.fixture.OrderItemFixture;
import dev.labs.commerce.order.core.order.domain.fixture.SalesOrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AbortOrderByStockFailureUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private AbortOrderByStockFailureUseCase abortOrderByStockFailureUseCase;

    @Test
    @DisplayName("CREATED 주문을 ABORTED로 전이한다")
    void execute_fromCreated_marksAborted() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.CREATED);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        abortOrderByStockFailureUseCase.execute(new AbortOrderByStockFailureCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ABORTED);
        assertThat(order.getAbortedAt()).isNotNull();
    }

    @Test
    @DisplayName("PENDING 주문도 ABORTED로 전이할 수 있다")
    void execute_fromPending_marksAborted() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.PENDING);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        abortOrderByStockFailureUseCase.execute(new AbortOrderByStockFailureCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.ABORTED);
    }

    @Test
    @DisplayName("CREATED/PENDING이 아닌 상태이면 아무 동작도 하지 않는다 (멱등)")
    void execute_whenNotCreatedOrPending_doesNothing() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = orderWithStatus(orderId, OrderStatus.PAID);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        abortOrderByStockFailureUseCase.execute(new AbortOrderByStockFailureCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    @DisplayName("주문을 찾지 못하면 OrderNotFoundException이 발생한다")
    void execute_whenOrderNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> abortOrderByStockFailureUseCase.execute(new AbortOrderByStockFailureCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);
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
