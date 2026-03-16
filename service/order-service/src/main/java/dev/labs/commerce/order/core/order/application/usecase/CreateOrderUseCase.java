package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderCommand;
import dev.labs.commerce.order.core.order.application.usecase.dto.CreateOrderResult;
import dev.labs.commerce.order.core.order.application.usecase.dto.OrderItemCommand;
import dev.labs.commerce.order.core.order.domain.OrderItem;
import dev.labs.commerce.order.core.order.domain.ProductInfo;
import dev.labs.commerce.order.core.order.domain.ProductPort;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderErrorCode;
import dev.labs.commerce.order.core.order.domain.error.OrderProductInvalidException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductPort productPort;

    public CreateOrderResult execute(CreateOrderCommand command) {
        Map<Long, ProductInfo> productMap = validateProducts(command);

        List<OrderItem> items = command.items().stream()
                .map(item -> OrderItem.create(
                        item.productId(),
                        productMap.get(item.productId()).productName(),
                        item.unitPrice(),
                        item.quantity(),
                        item.currency()
                ))
                .toList();

        SalesOrder order = SalesOrder.create(command.customerId(), command.currency(), items, Instant.now());
        SalesOrder saved = salesOrderRepository.save(order);

        return new CreateOrderResult(
                saved.getOrderId(),
                saved.getStatus(),
                saved.getTotalPrice(),
                saved.getTotalAmount(),
                saved.getCurrency()
        );
    }

    private Map<Long, ProductInfo> validateProducts(CreateOrderCommand command) {
        List<Long> productIds = command.items().stream()
                .map(OrderItemCommand::productId)
                .toList();

        Map<Long, ProductInfo> productMap = productPort.findProducts(productIds)
                .stream()
                .collect(Collectors.toMap(ProductInfo::productId, p -> p));

        long expectedTotalPrice = 0L;
        long expectedTotalAmount = 0L;

        for (OrderItemCommand item : command.items()) {
            ProductInfo product = productMap.get(item.productId());
            if (product == null) {
                throw new OrderProductInvalidException(
                        OrderErrorCode.PRODUCT_NOT_FOUND,
                        "Product not found. productId=" + item.productId()
                );
            }
            if (!"ACTIVE".equals(product.productStatus())) {
                throw new OrderProductInvalidException(
                        OrderErrorCode.PRODUCT_NOT_ACTIVE,
                        "Product is not available for order. productId=" + item.productId()
                );
            }

            if (item.unitPrice() != product.price()) {
                throw new OrderProductInvalidException(
                        OrderErrorCode.LINE_AMOUNT_MISMATCH,
                        "Unit price mismatch. productId=" + item.productId()
                                + ", expected=" + product.price()
                                + ", actual=" + item.unitPrice()
                );
            }

            long expectedLineAmount = product.price() * item.quantity();
            if (item.lineAmount() != expectedLineAmount) {
                throw new OrderProductInvalidException(
                        OrderErrorCode.LINE_AMOUNT_MISMATCH,
                        "Line amount mismatch. productId=" + item.productId()
                                + ", expected=" + expectedLineAmount
                                + ", actual=" + item.lineAmount()
                );
            }

            expectedTotalPrice += expectedLineAmount;
            expectedTotalAmount += item.quantity();
        }

        if (command.totalPrice() != expectedTotalPrice) {
            throw new OrderProductInvalidException(
                    OrderErrorCode.TOTAL_PRICE_MISMATCH,
                    "Total price mismatch. expected=" + expectedTotalPrice + ", actual=" + command.totalPrice()
            );
        }
        if (command.totalAmount() != expectedTotalAmount) {
            throw new OrderProductInvalidException(
                    OrderErrorCode.TOTAL_AMOUNT_MISMATCH,
                    "Total amount mismatch. expected=" + expectedTotalAmount + ", actual=" + command.totalAmount()
            );
        }

        return productMap;
    }
}
