package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ConfirmOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.domain.*;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConfirmOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public void execute(ConfirmOrderInventoryCommand command) {
        log.info("Confirming inventory: orderId={}, items={}",
                command.orderId(),
                command.items().stream()
                        .map(i -> "productId=" + i.productId() + ",qty=" + i.quantity())
                        .toList());
        for (ConfirmOrderInventoryCommand.Item item : command.items()) {
            if (inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                    command.orderId(), item.productId(), OperationType.CONFIRM)) {
                continue;
            }

            Inventory inventory = inventoryRepository.findById(item.productId())
                    .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

            inventory.confirm(item.quantity());

            inventoryHistoryRepository.save(
                    InventoryHistory.confirm(command.orderId(), inventory, item.quantity(), Actor.ORDER_SERVICE)
            );
        }
    }
}
