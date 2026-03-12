package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ConfirmOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;

    public void execute(ConfirmOrderInventoryCommand command) {
        for (ConfirmOrderInventoryCommand.Item item : command.items()) {
            Inventory inventory = inventoryRepository.findById(item.productId())
                    .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

            inventory.confirm(item.quantity());
        }
    }
}
