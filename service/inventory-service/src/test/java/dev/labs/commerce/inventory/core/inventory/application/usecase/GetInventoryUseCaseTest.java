package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.GetInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class GetInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private GetInventoryUseCase getInventoryUseCase;

    @Test
    @DisplayName("productId로 재고를 조회하면 전체/가용 수량을 담은 결과를 반환한다")
    void execute_returnsInventoryDetail() {
        // given
        final Long productId = 1L;
        final Inventory inventory = Inventory.create(productId);
        inventory.increase(10);
        inventory.reserve(3);
        given(inventoryRepository.findById(productId)).willReturn(Optional.of(inventory));

        // when
        final GetInventoryResult actual = getInventoryUseCase.execute(productId);

        // then
        assertThat(actual.productId()).isEqualTo(productId);
        assertThat(actual.totalQuantity()).isEqualTo(10);
        assertThat(actual.availableQuantity()).isEqualTo(7);
    }

    @Test
    @DisplayName("재고를 찾지 못하면 InventoryNotFoundException이 발생한다")
    void execute_whenInventoryNotFound_throwsException() {
        // given
        final Long productId = 999L;
        given(inventoryRepository.findById(productId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getInventoryUseCase.execute(productId))
                .isInstanceOf(InventoryNotFoundException.class);
    }
}
