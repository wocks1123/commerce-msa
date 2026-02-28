package dev.labs.commerce.inventory.api;

import dev.labs.commerce.common.web.doc.ApiBadRequestResponse;
import dev.labs.commerce.common.web.doc.ApiNotFoundResponse;
import dev.labs.commerce.inventory.api.dto.IncreaseInventoryQuantityRequest;
import dev.labs.commerce.inventory.api.dto.InventoryQuantityResponse;
import dev.labs.commerce.inventory.core.inventory.application.usecase.IncreaseInventoryQuantityUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory API")
public class InventoryRestController {

    private final IncreaseInventoryQuantityUseCase increaseInventoryQuantityUseCase;

    @Operation(summary = "Increase inventory stock quantity")
    @ApiResponse(responseCode = "200", description = "Inventory quantity increased successfully",
            content = @Content(schema = @Schema(implementation = InventoryQuantityResponse.class)))
    @ApiBadRequestResponse
    @ApiNotFoundResponse
    @PatchMapping("/{productId}/quantity")
    public InventoryQuantityResponse increaseInventoryQuantity(
            @PathVariable Long productId,
            @RequestBody IncreaseInventoryQuantityRequest request) {
        IncreaseInventoryQuantityCommand command = new IncreaseInventoryQuantityCommand(
                productId,
                request.quantity()
        );
        IncreaseInventoryQuantityResult result = increaseInventoryQuantityUseCase.execute(command);
        return new InventoryQuantityResponse(
                result.productId(),
                result.totalQuantity(),
                result.availableQuantity()
        );
    }
}
