package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReleaseOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.domain.Actor;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.OperationType;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReleaseOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;

    public void execute(ReleaseOrderInventoryCommand command) {
        log.info("Releasing inventory: orderId={}, productId={}, quantity={}",
                command.orderId(), command.productId(), command.quantity());

        Set<OperationType> ops = inventoryHistoryRepository.findOperationTypesByOrderIdAndProductId(
                command.orderId(), command.productId());

        if (ops.contains(OperationType.RELEASE)) {
            log.info("Already released, skipping: orderId={}, productId={}", command.orderId(), command.productId());
            return;
        }

        if (!ops.contains(OperationType.RESERVE)) {
            log.info("No RESERVE history found, skipping release: orderId={}, productId={}", command.orderId(), command.productId());
            return;
        }

        Inventory inventory = inventoryRepository.findById(command.productId())
                .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        inventory.release(command.quantity());

        inventoryHistoryRepository.save(
                InventoryHistory.release(command.orderId(), inventory, command.quantity(), Actor.ORDER_SERVICE)
        );
    }
}
