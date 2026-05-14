package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaymentInitializedCommand;
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
class ConfirmPaymentInitializedUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private ConfirmPaymentInitializedUseCase confirmPaymentInitializedUseCase;

    @Test
    @DisplayName("CREATED 주문을 PENDING으로 전이한다")
    void execute_fromCreated_marksPending() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = createdOrder(orderId);
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        confirmPaymentInitializedUseCase.execute(new ConfirmPaymentInitializedCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(order.getPendingAt()).isNotNull();
    }

    @Test
    @DisplayName("CREATED가 아닌 상태이면 아무 동작도 하지 않는다 (멱등)")
    void execute_whenNotCreated_doesNothing() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(OrderStatus.PENDING)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.of(order));

        // when
        confirmPaymentInitializedUseCase.execute(new ConfirmPaymentInitializedCommand(orderId));

        // then
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문을 찾지 못하면 OrderNotFoundException이 발생한다")
    void execute_whenOrderNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(salesOrderRepository.findByIdWithLock(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> confirmPaymentInitializedUseCase.execute(new ConfirmPaymentInitializedCommand(orderId)))
                .isInstanceOf(OrderNotFoundException.class);
    }

    private SalesOrder createdOrder(String orderId) {
        return SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(OrderStatus.CREATED)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
    }
}
