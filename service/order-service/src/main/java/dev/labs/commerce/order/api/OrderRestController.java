package dev.labs.commerce.order.api;

import dev.labs.commerce.order.api.dto.CreateOrderItemRequest;
import dev.labs.commerce.order.api.dto.CreateOrderRequest;
import dev.labs.commerce.order.api.dto.CreateOrderResponse;
import dev.labs.commerce.order.core.order.application.usecase.CreateOrderUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderResult;
import dev.labs.commerce.order.core.order.application.usecase.dto.OrderItemCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRestController {

    private final CreateOrderUseCase createOrderUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
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
}
