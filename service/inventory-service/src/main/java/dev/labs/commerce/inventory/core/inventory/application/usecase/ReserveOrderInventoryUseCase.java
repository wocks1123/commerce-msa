package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.event.StockEventPublisher;
import dev.labs.commerce.inventory.core.inventory.application.event.StockReservationFailedEvent;
import dev.labs.commerce.inventory.core.inventory.application.event.StockReservedEvent;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReserveOrderInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.ReserveOrderInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Actor;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryHistoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import dev.labs.commerce.inventory.core.inventory.domain.OperationType;
import dev.labs.commerce.inventory.core.inventory.domain.error.InsufficientStockException;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryErrorCode;
import dev.labs.commerce.inventory.core.inventory.domain.error.InventoryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReserveOrderInventoryUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryHistoryRepository inventoryHistoryRepository;
    private final StockEventPublisher stockEventPublisher;

    public ReserveOrderInventoryResult execute(ReserveOrderInventoryCommand command) {
        List<ReserveOrderInventoryResult.ItemResult> results = new ArrayList<>();

        for (ReserveOrderInventoryCommand.Item item : command.items()) {
            if (inventoryHistoryRepository.existsByOrderIdAndProductIdAndOperationType(
                    command.orderId(), item.productId(), OperationType.RESERVE)) {
                log.warn("Duplicate RESERVE detected. orderId={}, productId={}", command.orderId(), item.productId());
                continue;
            }

            Optional<Inventory> inventoryOpt = inventoryRepository.findById(item.productId());

            if (inventoryOpt.isEmpty()) {
                stockEventPublisher.publishStockReservationFailed(new StockReservationFailedEvent(
                        item.productId(),
                        command.orderId(),
                        item.quantity(),
                        InventoryErrorCode.INVENTORY_NOT_FOUND.getCode()
                ));
                throw new InventoryNotFoundException(InventoryErrorCode.INVENTORY_NOT_FOUND);
            }

            Inventory inventory = inventoryOpt.get();

            try {
                inventory.reserve(item.quantity());
                inventoryHistoryRepository.save(
                        InventoryHistory.reserve(command.orderId(), inventory, item.quantity(), Actor.ORDER_SERVICE)
                );
                stockEventPublisher.publishStockReserved(new StockReservedEvent(
                        inventory.getProductId(),
                        command.orderId(),
                        item.quantity(),
                        inventory.getAvailableQuantity()
                ));
            } catch (InsufficientStockException e) {
                stockEventPublisher.publishStockReservationFailed(new StockReservationFailedEvent(
                        inventory.getProductId(),
                        command.orderId(),
                        item.quantity(),
                        e.getErrorCode().getCode()
                ));
                throw e;
            }

            results.add(new ReserveOrderInventoryResult.ItemResult(
                    inventory.getProductId(),
                    inventory.getTotalQuantity(),
                    inventory.getAvailableQuantity()
            ));
        }

        return new ReserveOrderInventoryResult(command.orderId(), results);
    }

}
