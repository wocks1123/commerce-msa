package dev.labs.commerce.product.api;

import dev.labs.commerce.common.web.doc.ApiBadRequestResponse;
import dev.labs.commerce.common.web.doc.ApiConflictResponse;
import dev.labs.commerce.common.web.doc.ApiNotFoundResponse;
import dev.labs.commerce.product.api.dto.*;
import dev.labs.commerce.product.core.product.application.usecase.*;
import dev.labs.commerce.product.core.product.application.usecase.dto.*;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product Master", description = "Product Master API")
public class ProductRestController {

    private final RegisterProductUseCase registerProductUseCase;
    private final ModifyProductUseCase modifyProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final ListProductsByIdsUseCase listProductsByIdsUseCase;

    @Operation(summary = "Register new product")
    @ApiResponse(responseCode = "201", description = "Product registered successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiBadRequestResponse
    @ApiConflictResponse
    @PostMapping
    public ResponseEntity<ProductResponse> registerProduct(@RequestBody RegisterProductRequest request) {
        RegisterProductCommand command = new RegisterProductCommand(
                request.productName(),
                request.price(),
                request.currency(),
                request.description()
        );
        RegisterProductResult result = registerProductUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(toProductResponse(result));
    }

    @Operation(summary = "Modify product details")
    @ApiResponse(responseCode = "200", description = "Product modified successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiBadRequestResponse
    @ApiNotFoundResponse
    @PutMapping("/{productId}")
    public ProductResponse modifyProduct(@PathVariable Long productId,
                                         @RequestBody ModifyProductRequest request) {
        ModifyProductCommand command = new ModifyProductCommand(
                productId,
                request.productName(),
                request.price(),
                request.currency(),
                request.description()
        );
        ModifyProductResult result = modifyProductUseCase.execute(command);
        return toProductResponse(result);
    }

    @Operation(summary = "Change product status")
    @ApiResponse(responseCode = "200", description = "Product status changed successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiBadRequestResponse
    @ApiNotFoundResponse
    @PatchMapping("/{productId}/status")
    public ProductResponse changeProductStatus(@PathVariable Long productId,
                                               @RequestBody ChangeProductStatusRequest request) {
        ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, request.status());
        ChangeProductStatusResult result = changeProductStatusUseCase.execute(command);
        return toProductResponse(result);
    }

    @Operation(summary = "Get product details")
    @ApiResponse(responseCode = "200", description = "Product details retrieved successfully", content = @Content(schema = @Schema(implementation = ProductResponse.class)))
    @ApiBadRequestResponse
    @ApiNotFoundResponse
    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        GetProductResult result = getProductUseCase.execute(productId);
        return toProductResponse(result);
    }

    @Operation(summary = "List all products")
    @ApiResponse(responseCode = "200", description = "Products listed successfully", content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    @ApiBadRequestResponse
    @GetMapping
    public List<ProductSummaryResponse> listProducts(@RequestParam(required = false) ProductStatus status) {
        return listProductsUseCase.execute(status)
                .stream()
                .map(this::toProductSummaryResponse)
                .collect(Collectors.toList());
    }

    @Operation(summary = "List products by IDs")
    @ApiResponse(responseCode = "200", description = "Products retrieved successfully", content = @Content(schema = @Schema(implementation = ProductSummaryResponse.class)))
    @ApiBadRequestResponse
    @GetMapping(params = "ids")
    public List<ProductSummaryResponse> listProductsByIds(@RequestParam List<Long> ids) {
        return listProductsByIdsUseCase.execute(ids)
                .stream()
                .map(this::toProductSummaryResponse)
                .collect(Collectors.toList());
    }

    private ProductResponse toProductResponse(RegisterProductResult result) {
        return new ProductResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private ProductResponse toProductResponse(ModifyProductResult result) {
        return new ProductResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private ProductResponse toProductResponse(ChangeProductStatusResult result) {
        return new ProductResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private ProductResponse toProductResponse(GetProductResult result) {
        return new ProductResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus(),
                result.description(),
                result.createdAt(),
                result.updatedAt()
        );
    }

    private ProductSummaryResponse toProductSummaryResponse(ListProductsResult result) {
        return new ProductSummaryResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus()
        );
    }

    private ProductSummaryResponse toProductSummaryResponse(ListProductsByIdsResult result) {
        return new ProductSummaryResponse(
                result.productId(),
                result.productName(),
                result.price(),
                result.currency(),
                result.productStatus()
        );
    }
}
