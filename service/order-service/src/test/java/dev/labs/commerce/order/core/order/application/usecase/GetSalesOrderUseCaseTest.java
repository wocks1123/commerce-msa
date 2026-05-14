package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.GetSalesOrderResult;
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
class GetSalesOrderUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @InjectMocks
    private GetSalesOrderUseCase getSalesOrderUseCase;

    @Test
    @DisplayName("orderId로 주문을 조회하면 주문 상세를 반환한다")
    void execute_returnsOrderDetail() {
        // given
        final String orderId = "order-1";
        final SalesOrder order = SalesOrderFixture.builder()
                .withSample()
                .orderId(orderId)
                .status(OrderStatus.CREATED)
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
        given(salesOrderRepository.findById(orderId)).willReturn(Optional.of(order));

        // when
        final GetSalesOrderResult actual = getSalesOrderUseCase.execute(orderId);

        // then
        assertThat(actual.orderId()).isEqualTo(orderId);
        assertThat(actual.status()).isEqualTo(OrderStatus.CREATED);
        assertThat(actual.items()).hasSize(1);
    }

    @Test
    @DisplayName("주문을 찾지 못하면 OrderNotFoundException이 발생한다")
    void execute_whenOrderNotFound_throwsException() {
        // given
        final String orderId = "missing";
        given(salesOrderRepository.findById(orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getSalesOrderUseCase.execute(orderId))
                .isInstanceOf(OrderNotFoundException.class);
    }
}
