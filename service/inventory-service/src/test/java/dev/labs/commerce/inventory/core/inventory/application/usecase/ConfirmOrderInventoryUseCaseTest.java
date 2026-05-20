package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ConfirmOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.domain.Actor;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.OperationType;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @InjectMocks
    private ConfirmOrderInventoryUseCase confirmOrderInventoryUseCase;

    @Test
    @DisplayName("예약된 재고를 확정하면 예약/전체 수량이 함께 감소하고 CONFIRM 이력이 저장된다")
    void execute_confirmsReservedInventory_andSavesHistory() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        inventory.reserve(4);
        final ConfirmOrderInventoryCommand command = new ConfirmOrderInventoryCommand(
                orderId,
                List.of(new ConfirmOrderInventoryCommand.Item(productId, 4))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.CONFIRM)).willReturn(false);
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when
        confirmOrderInventoryUseCase.execute(command);

        // then
        assertThat(inventory.getTotalQuantity()).isEqualTo(6);
        assertThat(inventory.getReservedQuantity()).isZero();
        final ArgumentCaptor<InventoryHistory> captor = ArgumentCaptor.forClass(InventoryHistory.class);
        then(inventoryHistoryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getOperationType()).isEqualTo(OperationType.CONFIRM);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getQuantity()).isEqualTo(4);
        assertThat(captor.getValue().getActor()).isEqualTo(Actor.ORDER_SERVICE);
    }

    @Test
    @DisplayName("이미 CONFIRM 이력이 있는 항목은 건너뛴다")
    void execute_whenAlreadyConfirmed_skipsItem() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final ConfirmOrderInventoryCommand command = new ConfirmOrderInventoryCommand(
                orderId,
                List.of(new ConfirmOrderInventoryCommand.Item(productId, 4))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.CONFIRM)).willReturn(true);

        // when
        confirmOrderInventoryUseCase.execute(command);

        // then
        then(inventoryRepository).should(never()).findById(any());
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }

    @Test
    @DisplayName("재고를 찾지 못하면 InventoryNotFoundException이 발생한다")
    void execute_whenInventoryNotFound_throwsException() {
        // given
        final String orderId = "order-1";
        final Long productId = 999L;
        final ConfirmOrderInventoryCommand command = new ConfirmOrderInventoryCommand(
                orderId,
                List.of(new ConfirmOrderInventoryCommand.Item(productId, 4))
        );
        given(inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                orderId, productId, OperationType.CONFIRM)).willReturn(false);
        given(inventoryRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> confirmOrderInventoryUseCase.execute(command))
                .isInstanceOf(InventoryNotFoundException.class);
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }
}
