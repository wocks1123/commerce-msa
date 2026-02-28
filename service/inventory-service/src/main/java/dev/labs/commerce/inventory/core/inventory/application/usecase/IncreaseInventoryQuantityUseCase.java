package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IncreaseInventoryQuantityUseCase {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public IncreaseInventoryQuantityResult execute(IncreaseInventoryQuantityCommand command) {
        Inventory inventory = inventoryRepository.findById(command.productId())
                .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.increase(command.quantity());

        return new IncreaseInventoryQuantityResult(
                inventory.getProductId(),
                inventory.getTotalQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}
