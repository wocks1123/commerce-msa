package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseInventoryQuantityResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DecreaseInventoryQuantityUseCase {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public DecreaseInventoryQuantityResult execute(DecreaseInventoryQuantityCommand command) {
        Inventory inventory = inventoryRepository.findById(command.productId())
                .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.decrease(command.quantity());

        return new DecreaseInventoryQuantityResult(
                inventory.getProductId(),
                inventory.getTotalQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}
