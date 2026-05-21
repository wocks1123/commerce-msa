package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityResult;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class IncreaseInventoryQuantityUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @InjectMocks
    private IncreaseInventoryQuantityUseCase increaseInventoryQuantityUseCase;

    @Test
    @DisplayName("입고 수량만큼 재고가 증가하고 RESTOCK 이력이 저장된다")
    void execute_increasesInventoryAndSavesHistory() {
        // given
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(5);
        final IncreaseInventoryQuantityCommand command = new IncreaseInventoryQuantityCommand(productId, 7);
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when
        final IncreaseInventoryQuantityResult actual = increaseInventoryQuantityUseCase.execute(command);

        // then
        assertThat(actual.productId()).isEqualTo(productId);
        assertThat(actual.totalQuantity()).isEqualTo(12);
        assertThat(actual.availableQuantity()).isEqualTo(12);
        final ArgumentCaptor<InventoryHistory> captor = ArgumentCaptor.forClass(InventoryHistory.class);
        then(inventoryHistoryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getOperationType()).isEqualTo(OperationType.RESTOCK);
        assertThat(captor.getValue().getQuantity()).isEqualTo(7);
        assertThat(captor.getValue().getActor()).isEqualTo(Actor.ADMIN);
    }

    @Test
    @DisplayName("재고를 찾지 못하면 InventoryNotFoundException이 발생하고 이력은 저장되지 않는다")
    void execute_whenInventoryNotFound_throwsException() {
        // given
        final Long productId = 999L;
        final IncreaseInventoryQuantityCommand command = new IncreaseInventoryQuantityCommand(productId, 5);
        given(inventoryRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> increaseInventoryQuantityUseCase.execute(command))
                .isInstanceOf(InventoryNotFoundException.class);
        then(inventoryHistoryRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }
}
