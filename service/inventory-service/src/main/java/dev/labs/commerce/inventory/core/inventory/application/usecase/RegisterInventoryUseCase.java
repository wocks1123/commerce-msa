package dev.labs.commerce.inventory.core.inventory.application.usecase;

import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RegisterInventoryCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.RegisterInventoryResult;
import dev.labs.commerce.inventory.core.inventory.domain.Inventory;
import dev.labs.commerce.inventory.core.inventory.domain.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RegisterInventoryUseCase {

    private final InventoryRepository inventoryRepository;


    @Transactional
    public RegisterInventoryResult execute(RegisterInventoryCommand command) {
        if (inventoryRepository.existsById(command.productId())) {
            log.info("Inventory already exists, skipping: productId={}", command.productId());
            return new RegisterInventoryResult(command.productId());
        }
        Inventory inventory = Inventory.create(command.productId());
        Inventory saved = inventoryRepository.save(inventory);
        return new RegisterInventoryResult(saved.getProductId());
    }

}
