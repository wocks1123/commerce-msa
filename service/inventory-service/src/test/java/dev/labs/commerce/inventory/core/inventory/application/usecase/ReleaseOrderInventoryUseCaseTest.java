package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReleaseOrderInventoryCommand;
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

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ReleaseOrderInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @InjectMocks
    private ReleaseOrderInventoryUseCase releaseOrderInventoryUseCase;

    @Test
    @DisplayName("RESERVE 이력이 있고 RELEASE 이력이 없으면 예약을 해제하고 RELEASE 이력을 저장한다")
    void execute_releasesReservation_andSavesHistory() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        inventory.reserve(4);
        final ReleaseOrderInventoryCommand command = new ReleaseOrderInventoryCommand(productId, orderId, 4);
        given(inventoryHistoryRepository.findOperationTypesByOrderIdAndProductId(orderId, productId))
                .willReturn(EnumSet.of(OperationType.RESERVE));
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when
        releaseOrderInventoryUseCase.execute(command);

        // then
        assertThat(inventory.getReservedQuantity()).isZero();
        assertThat(inventory.getAvailableQuantity()).isEqualTo(10);
        final ArgumentCaptor<InventoryHistory> captor = ArgumentCaptor.forClass(InventoryHistory.class);
        then(inventoryHistoryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getOperationType()).isEqualTo(OperationType.RELEASE);
        assertThat(captor.getValue().getOrderId()).isEqualTo(orderId);
        assertThat(captor.getValue().getQuantity()).isEqualTo(4);
        assertThat(captor.getValue().getActor()).isEqualTo(Actor.ORDER_SERVICE);
    }

    @Test
    @DisplayName("이미 RELEASE 이력이 있으면 아무 작업도 하지 않는다")
    void execute_whenAlreadyReleased_skips() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final ReleaseOrderInventoryCommand command = new ReleaseOrderInventoryCommand(productId, orderId, 4);
        given(inventoryHistoryRepository.findOperationTypesByOrderIdAndProductId(orderId, productId))
                .willReturn(EnumSet.of(OperationType.RESERVE, OperationType.RELEASE));

        // when
        releaseOrderInventoryUseCase.execute(command);

        // then
        then(inventoryRepository).should(never()).findById(any());
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }

    @Test
    @DisplayName("RESERVE 이력이 없으면 해제 작업을 건너뛴다")
    void execute_whenNoReserveHistory_skips() {
        // given
        final String orderId = "order-1";
        final Long productId = 1L;
        final ReleaseOrderInventoryCommand command = new ReleaseOrderInventoryCommand(productId, orderId, 4);
        final Set<OperationType> emptyOps = EnumSet.noneOf(OperationType.class);
        given(inventoryHistoryRepository.findOperationTypesByOrderIdAndProductId(orderId, productId))
                .willReturn(emptyOps);

        // when
        releaseOrderInventoryUseCase.execute(command);

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
        final ReleaseOrderInventoryCommand command = new ReleaseOrderInventoryCommand(productId, orderId, 4);
        given(inventoryHistoryRepository.findOperationTypesByOrderIdAndProductId(orderId, productId))
                .willReturn(EnumSet.of(OperationType.RESERVE));
        given(inventoryRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> releaseOrderInventoryUseCase.execute(command))
                .isInstanceOf(InventoryNotFoundException.class);
        then(inventoryHistoryRepository).should(never()).save(any(InventoryHistory.class));
    }
}
