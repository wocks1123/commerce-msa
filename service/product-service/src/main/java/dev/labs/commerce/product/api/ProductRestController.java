package dev.labs.commerce.product.api;

import dev.labs.commerce.product.api.dto.*;
import dev.labs.commerce.product.core.product.application.usecase.ChangeProductStatusUseCase;
import dev.labs.commerce.product.core.product.application.usecase.GetProductUseCase;
import dev.labs.commerce.product.core.product.application.usecase.ListProductsUseCase;
import dev.labs.commerce.product.core.product.application.usecase.ModifyProductUseCase;
import dev.labs.commerce.product.core.product.application.usecase.RegisterProductUseCase;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ChangeProductStatusResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.GetProductResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.ListProductsResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.ModifyProductResult;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductCommand;
import dev.labs.commerce.product.core.product.application.usecase.dto.RegisterProductResult;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductRestController {

    private final RegisterProductUseCase registerProductUseCase;
    private final ModifyProductUseCase modifyProductUseCase;
    private final ChangeProductStatusUseCase changeProductStatusUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;

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

    @PatchMapping("/{productId}/status")
    public ProductResponse changeProductStatus(@PathVariable Long productId,
                                               @RequestBody ChangeProductStatusRequest request) {
        ChangeProductStatusCommand command = new ChangeProductStatusCommand(productId, request.status());
        ChangeProductStatusResult result = changeProductStatusUseCase.execute(command);
        return toProductResponse(result);
    }

    @GetMapping("/{productId}")
    public ProductResponse getProduct(@PathVariable Long productId) {
        GetProductResult result = getProductUseCase.execute(productId);
        return toProductResponse(result);
    }

    @GetMapping
    public List<ProductSummaryResponse> listProducts(@RequestParam(required = false) ProductStatus status) {
        return listProductsUseCase.execute(status)
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
}
