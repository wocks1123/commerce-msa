package dev.labs.commerce.inventory.api.http;

import dev.labs.commerce.common.web.doc.ApiBadRequestResponse;
import dev.labs.commerce.common.web.doc.ApiNotFoundResponse;
import dev.labs.commerce.inventory.api.http.dto.IncreaseInventoryQuantityRequest;
import dev.labs.commerce.inventory.api.http.dto.InventoryQuantityResponse;
import dev.labs.commerce.inventory.core.inventory.application.usecase.GetInventoryUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.IncreaseInventoryQuantityUseCase;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.GetInventoryResult;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityCommand;
import dev.labs.commerce.inventory.core.inventory.application.usecase.dto.IncreaseInventoryQuantityResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory API")
public class InventoryRestController {

    private final GetInventoryUseCase getInventoryUseCase;
    private final IncreaseInventoryQuantityUseCase increaseInventoryQuantityUseCase;

    @Operation(summary = "Get inventory by productId")
    @ApiResponse(responseCode = "200", description = "Inventory retrieved successfully",
            content = @Content(schema = @Schema(implementation = InventoryQuantityResponse.class)))
    @ApiNotFoundResponse
    @GetMapping("/{productId}")
    public InventoryQuantityResponse getInventory(@PathVariable Long productId) {
        GetInventoryResult result = getInventoryUseCase.execute(productId);
        return new InventoryQuantityResponse(
                result.productId(),
                result.totalQuantity(),
                result.availableQuantity()
        );
    }

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
