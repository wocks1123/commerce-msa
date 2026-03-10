package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.DecreaseOrderInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockDeductionFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class DecreaseOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final StockEventPublisher stockEventPublisher;

    public DecreaseOrderInventoryResult execute(DecreaseOrderInventoryCommand command) {
        List<DecreaseOrderInventoryResult.ItemResult> results = new ArrayList<>();

        for (DecreaseOrderInventoryCommand.Item item : command.items()) {
            Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.productId());

            if (inventoryOpt.isEmpty()) {
                stockEventPublisher.publishStockDeductionFailed(new StockDeductionFailedEvent(
                        item.productId(),
                        command.orderId(),
                        item.quantity(),
                        InventoryErrorCode.INVENTORY_NOT_FOUND.getCode()
                ));
                throw new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND);
            }

            Inventory inventory = inventoryOpt.get();

            try {
                inventory.decrease(item.quantity());
                stockEventPublisher.publishStockDeducted(new StockDeductedEvent(
                        inventory.getProductId(),
                        command.orderId(),
                        item.quantity(),
                        inventory.getAvailableQuantity()
                ));
            } catch (InsufficientStockException e) {
                stockEventPublisher.publishStockDeductionFailed(new StockDeductionFailedEvent(
                        inventory.getProductId(),
                        command.orderId(),
                        item.quantity(),
                        e.getErrorCode().getCode()
                ));
                throw e;
            }

            results.add(new DecreaseOrderInventoryResult.ItemResult(
                    inventory.getProductId(),
                    inventory.getTotalQuantity(),
                    inventory.getAvailableQuantity()
            ));
        }

        return new DecreaseOrderInventoryResult(command.orderId(), results);
    }

}
