package dev.labs.commerce.order.api.http;

import dev.labs.commerce.order.api.http.dto.CreateOrderRequest;
import dev.labs.commerce.order.api.http.dto.CreateOrderResponse;
import dev.labs.commerce.order.api.http.dto.GetSalesOrderResponse;
import dev.labs.commerce.order.core.order.application.usecase.CreateOrderUseCase;
import dev.labs.commerce.order.core.order.application.usecase.GetSalesOrderUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderResult;
import dev.labs.commerce.order.core.order.application.usecase.dto.GetSalesOrderResult;
import dev.labs.commerce.order.core.order.application.usecase.dto.OrderItemCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetSalesOrderUseCase getSalesOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request) {
        List<OrderItemCommand> itemCommands = request.items().stream()
                .map(item -> new OrderItemCommand(
                        item.productId(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineAmount(),
                        item.currency()
                ))
                .toList();

        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                request.currency(),
                request.totalPrice(),
                request.totalAmount(),
                itemCommands
        );

        CreateOrderResult result = createOrderUseCase.execute(command);

        return new CreateOrderResponse(
                result.orderId(),
                result.status().name(),
                result.totalPrice(),
                result.totalAmount(),
                result.currency()
        );
    }

    @GetMapping("/{orderId}")
    public GetSalesOrderResponse getSalesOrder(@PathVariable String orderId) {
        GetSalesOrderResult result = getSalesOrderUseCase.execute(orderId);
        return toGetSalesOrderResponse(result);
    }

    private GetSalesOrderResponse toGetSalesOrderResponse(GetSalesOrderResult result) {
        return new GetSalesOrderResponse(
                result.orderId(),
                result.customerId(),
                result.status(),
                result.totalPrice(),
                result.totalAmount(),
                result.currency(),
                result.items().stream()
                        .map(item -> new GetSalesOrderResponse.OrderItemResponse(
                                item.orderItemId(),
                                item.productId(),
                                item.productName(),
                                item.unitPrice(),
                                item.quantity(),
                                item.lineAmount(),
                                item.currency()
                        ))
                        .toList(),
                result.pendingAt(),
                result.paidAt(),
                result.abortedAt(),
                result.cancelledAt(),
                result.failedAt(),
                result.expiredAt()
        );
    }
}
