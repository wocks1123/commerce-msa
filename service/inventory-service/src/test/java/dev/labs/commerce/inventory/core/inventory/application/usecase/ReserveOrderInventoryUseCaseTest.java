package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.event.StockEventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockReservationFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReserveOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReserveOrderInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Actor;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.OperationType;
import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
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
class ReserveOrderInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private StockEventPublisher stockEventPublisher;

    @InjectMocks
    private ReserveOrderInventoryUseCase reserveOrderInventoryUseCase;

    @Test
    @DisplayName("재고가 충분하면 항목별로 예약하고 RESERVE 이력을 저장한 뒤 결과를 반환한다")
    void execute_reservesAllItems_andSavesHistory() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        final ReserveOrderInventoryCommand command = new ReserveOrderInventoryCommand(
                orderId,
                List.of(new ReserveOrderInventoryCommand.Item(productId, 3))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.RESERVE)).willReturn(false);
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when
        final ReserveOrderInventoryResult actual = reserveOrderInventoryUseCase.execute(command);

        // then
        assertThat(actual.orderId()).isEqualTo(orderId);
        assertThat(actual.items()).hasSize(1);
        assertThat(actual.items().get(0).productId()).isEqualTo(productId);
        assertThat(actual.items().get(0).totalQuantity()).isEqualTo(10);
        assertThat(actual.items().get(0).availableQuantity()).isEqualTo(7);
        final ArgumentCaptor<InventoryHistory> captor = ArgumentCaptor.forClass(InventoryHistory.class);
        then(inventoryHistoryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getOperationType()).isEqualTo(OperationType.RESERVE);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getQuantity()).isEqualTo(3);
        assertThat(captor.getValue().getActor()).isEqualTo(Actor.ORDER_SERVICE);
        then(stockEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("이미 RESERVE 이력이 있는 항목은 건너뛴다")
    void execute_whenAlreadyReserved_skipsItem() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final ReserveOrderInventoryCommand command = new ReserveOrderInventoryCommand(
                orderId,
                List.of(new ReserveOrderInventoryCommand.Item(productId, 3))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.RESERVE)).willReturn(true);

        // when
        final ReserveOrderInventoryResult actual = reserveOrderInventoryUseCase.execute(command);

        // then
        assertThat(actual.orderId()).isEqualTo(orderId);
        assertThat(actual.items()).isEmpty();
        then(inventoryRepository).should(never()).findById(any());
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
        then(stockEventPublisher).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("재고를 찾지 못하면 실패 이벤트를 발행하고 InventoryNotFoundException이 발생한다")
    void execute_whenInventoryNotFound_publishesFailureAndThrows() {
        // given
        final String orderId = "order-1";
        final Long productId = 999L;
        final ReserveOrderInventoryCommand command = new ReserveOrderInventoryCommand(
                orderId,
                List.of(new ReserveOrderInventoryCommand.Item(productId, 3))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.RESERVE)).willReturn(false);
        given(inventoryRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reserveOrderInventoryUseCase.execute(command))
                .isInstanceOf(InventoryNotFoundException.class);
        final ArgumentCaptor<StockReservationFailedEvent> captor =
                ArgumentCaptor.forClass(StockReservationFailedEvent.class);
        then(stockEventPublisher).should().publishStockReservationFailed(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().quantity()).isEqualTo(3);
        assertThat(captor.getValue().errorCode()).isEqualTo(InventoryErrorCode.INVENTORY_NOT_FOUND.getCode());
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }

    @Test
    @DisplayName("재고가 부족하면 실패 이벤트를 발행하고 InsufficientStockException이 발생한다")
    void execute_whenInsufficientStock_publishesFailureAndThrows() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(2);
        final ReserveOrderInventoryCommand command = new ReserveOrderInventoryCommand(
                orderId,
                List.of(new ReserveOrderInventoryCommand.Item(productId, 5))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.RESERVE)).willReturn(false);
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when & then
        assertThatThrownBy(() -> reserveOrderInventoryUseCase.execute(command))
                .isInstanceOf(InsufficientStockException.class);
        final ArgumentCaptor<StockReservationFailedEvent> captor =
                ArgumentCaptor.forClass(StockReservationFailedEvent.class);
        then(stockEventPublisher).should().publishStockReservationFailed(captor.capture());
        assertThat(captor.getValue().productId()).isEqualTo(productId);
        assertThat(captor.getValue().orderId()).isEqualTo(orderId);
        assertThat(captor.getValue().errorCode()).isEqualTo(InventoryErrorCode.INSUFFICIENT_STOCK.getCode());
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }
}
