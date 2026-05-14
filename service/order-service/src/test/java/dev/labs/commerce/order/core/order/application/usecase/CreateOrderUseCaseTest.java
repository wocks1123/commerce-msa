package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderResult;
import dev.labs.commerce.order.core.order.application.usecase.dto.OrderItemCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.ProductInfo;
import dev.labs.commerce.order.core.order.domain.ProductPort;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderProductInvalidException;
import dev.labs.commerce.order.core.order.domain.fixture.OrderItemFixture;
import dev.labs.commerce.order.core.order.domain.fixture.SalesOrderFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class CreateOrderUseCaseTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ProductPort productPort;

    @InjectMocks
    private CreateOrderUseCase createOrderUseCase;

    @Test
    @DisplayName("유효한 명령으로 주문을 생성하면 저장된 주문 정보를 반환한다")
    void execute_createsOrderAndReturnsResult() {
        // given
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final ProductInfo p2 = new ProductInfo(20L, "상품B", 2000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 5000L, 3L,
                List.of(
                        new OrderItemCommand(10L, 1000L, 1, 1000L, "KRW"),
                        new OrderItemCommand(20L, 2000L, 2, 4000L, "KRW")
                )
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1, p2));
        final SalesOrder saved = SalesOrderFixture.builder()
                .withSample()
                .orderId("order-1")
                .status(OrderStatus.CREATED)
                .totalPrice(5000L)
                .totalAmount(3L)
                .currency("KRW")
                .items(List.of(OrderItemFixture.builder().withSample().build()))
                .build();
        given(salesOrderRepository.save(any(SalesOrder.class))).willReturn(saved);

        // when
        final CreateOrderResult actual = createOrderUseCase.execute(command);

        // then
        assertThat(actual.orderId()).isEqualTo("order-1");
        assertThat(actual.status()).isEqualTo(OrderStatus.CREATED);
        then(salesOrderRepository).should().save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("상품 조회 결과에 productId가 없으면 PRODUCT_NOT_FOUND가 발생한다")
    void execute_whenProductNotFound_throwsException() {
        // given - findProducts에 productId=20가 누락됨
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 5000L, 3L,
                List.of(
                        new OrderItemCommand(10L, 1000L, 1, 1000L, "KRW"),
                        new OrderItemCommand(20L, 2000L, 2, 4000L, "KRW")
                )
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
        then(salesOrderRepository).should(never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("상품이 ACTIVE 상태가 아니면 PRODUCT_NOT_ACTIVE가 발생한다")
    void execute_whenProductInactive_throwsException() {
        // given
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "INACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 1000L, 1L,
                List.of(new OrderItemCommand(10L, 1000L, 1, 1000L, "KRW"))
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
        then(salesOrderRepository).should(never()).save(any(SalesOrder.class));
    }

    @Test
    @DisplayName("단가가 상품의 판매가와 다르면 LINE_AMOUNT_MISMATCH가 발생한다")
    void execute_whenUnitPriceMismatch_throwsException() {
        // given - sellingPrice=1000인데 unitPrice=999
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 999L, 1L,
                List.of(new OrderItemCommand(10L, 999L, 1, 999L, "KRW"))
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
    }

    @Test
    @DisplayName("라인 합계가 단가 × 수량과 다르면 LINE_AMOUNT_MISMATCH가 발생한다")
    void execute_whenLineAmountMismatch_throwsException() {
        // given - 1000 * 2 = 2000인데 lineAmount=1999
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 1999L, 2L,
                List.of(new OrderItemCommand(10L, 1000L, 2, 1999L, "KRW"))
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
    }

    @Test
    @DisplayName("totalPrice가 라인 합계의 합과 다르면 TOTAL_PRICE_MISMATCH가 발생한다")
    void execute_whenTotalPriceMismatch_throwsException() {
        // given - 라인합 = 1000, 그러나 totalPrice=999
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 999L, 1L,
                List.of(new OrderItemCommand(10L, 1000L, 1, 1000L, "KRW"))
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
    }

    @Test
    @DisplayName("totalAmount가 수량 합계와 다르면 TOTAL_AMOUNT_MISMATCH가 발생한다")
    void execute_whenTotalAmountMismatch_throwsException() {
        // given - 수량합 = 1, 그러나 totalAmount=2
        final ProductInfo p1 = new ProductInfo(10L, "상품A", 1000L, "KRW", "ACTIVE");
        final CreateOrderCommand command = new CreateOrderCommand(
                100L, "KRW", 1000L, 2L,
                List.of(new OrderItemCommand(10L, 1000L, 1, 1000L, "KRW"))
        );
        given(productPort.findProducts(anyList())).willReturn(List.of(p1));

        // when & then
        assertThatThrownBy(() -> createOrderUseCase.execute(command))
                .isInstanceOf(OrderProductInvalidException.class);
    }
}
