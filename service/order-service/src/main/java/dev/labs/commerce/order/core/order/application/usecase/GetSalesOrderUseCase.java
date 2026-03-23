package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.GetSalesOrderResult;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetSalesOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;

    @Transactional(readOnly = true)
    public GetSalesOrderResult execute(String orderId) {
        SalesOrder order = salesOrderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);

        return new GetSalesOrderResult(
                order.getOrderId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalPrice(),
                order.getTotalAmount(),
                order.getCurrency(),
                order.getItems().stream()
                        .map(item -> new GetSalesOrderResult.OrderItemResult(
                                item.getOrderItemId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getLineAmount(),
                                item.getCurrency()
                        ))
                        .toList(),
                order.getPendingAt(),
                order.getPaidAt(),
                order.getAbortedAt(),
                order.getCancelledAt(),
                order.getFailedAt(),
                order.getExpiredAt()
        );
    }
}
