package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReleaseOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReleaseOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public void execute(ReleaseOrderInventoryCommand command) {
        Inventory inventory = inventoryRepository.findById(command.productId())
                .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.release(command.quantity());
    }
}
