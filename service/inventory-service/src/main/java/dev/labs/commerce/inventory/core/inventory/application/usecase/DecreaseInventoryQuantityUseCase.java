package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductionFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseInventoryQuantityResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Deprecated(forRemoval = true)
@Service
@RequiredArgsConstructor
public class DecreaseInventoryQuantityUseCase {

    private final InventoryRepository inventoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public DecreaseInventoryQuantityResult execute(DecreaseInventoryQuantityCommand command) {
        Inventory inventory = inventoryRepository.findById(command.productId())
                .orElseThrow(() -> new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND));

        try {
            inventory.decrease(command.quantity());
            eventPublisher.publishEvent(new StockDeductedEvent(
                    inventory.getProductId(),
                    command.orderId(),
                    command.quantity(),
                    inventory.getAvailableQuantity()
            ));
        } catch (InsufficientStockException e) {
            eventPublisher.publishEvent(new StockDeductionFailedEvent(
                    inventory.getProductId(),
                    command.orderId(),
                    command.quantity(),
                    e.getErrorCode().getCode()
            ));
            throw e;
        }

        return new DecreaseInventoryQuantityResult(
                inventory.getProductId(),
                inventory.getTotalQuantity(),
                inventory.getAvailableQuantity()
        );
    }
}
