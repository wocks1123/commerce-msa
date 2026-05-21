package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RegisterInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RegisterInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RegisterInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private RegisterInventoryUseCase registerInventoryUseCase;

    @Test
    @DisplayName("재고가 존재하지 않으면 새로 생성하여 저장한다")
    void execute_whenInventoryNotExists_createsAndSaves() {
        // given
        final Long productId = 1L;
        final RegisterInventoryCommand command = new RegisterInventoryCommand(productId);
        given(inventoryRepository.existsById(productId)).willReturn(false);
        final Inventory result = Inventory.create(productId);
        given(inventoryRepository.save(any(Inventory.class))).willReturn(result);

        // when
        final RegisterInventoryResult actual = registerInventoryUseCase.execute(command);

        // then
        assertThat(actual.productId()).isEqualTo(productId);
        final ArgumentCaptor<Inventory> captor = ArgumentCaptor.forClass(Inventory.class);
        then(inventoryRepository).should().save(captor.capture());
        assertThat(captor.getValue().getProductId()).isEqualTo(productId);
    }

    @Test
    @DisplayName("이미 재고가 존재하면 저장하지 않고 productId만 반환한다")
    void execute_whenInventoryAlreadyExists_skipsSave() {
        // given
        final Long productId = 1L;
        final RegisterInventoryCommand command = new RegisterInventoryCommand(productId);
        given(inventoryRepository.existsById(productId)).willReturn(true);

        // when
        final RegisterInventoryResult actual = registerInventoryUseCase.execute(command);

        // then
        assertThat(actual.productId()).isEqualTo(productId);
        then(inventoryRepository).should(never()).save(any(Inventory.class));
    }
}
