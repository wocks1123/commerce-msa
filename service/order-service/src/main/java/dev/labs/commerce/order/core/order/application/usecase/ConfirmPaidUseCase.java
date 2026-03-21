package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.event.OrderPaidEvent;
import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaidCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderErrorCode;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import dev.labs.commerce.order.core.order.domain.error.OrderPaymentMismatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ConfirmPaidUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public void execute(ConfirmPaidCommand command) {
        SalesOrder order = salesOrderRepository.findByIdWithLock(command.orderId())
                .orElseThrow(OrderNotFoundException::new);

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in {} state, skipping confirm paid",
                    command.orderId(), order.getStatus());
            return;
        }

        if (order.getCustomerId() != command.customerId()) {
            throw new OrderPaymentMismatchException(OrderErrorCode.CUSTOMER_ID_MISMATCH);
        }
        if (order.getTotalPrice() != command.amount()) {
            throw new OrderPaymentMismatchException(OrderErrorCode.TOTAL_PRICE_MISMATCH);
        }
        if (!order.getCurrency().equals(command.currency())) {
            throw new OrderPaymentMismatchException(OrderErrorCode.CURRENCY_MISMATCH);
        }

        order.confirmPaid(Instant.now());

        List<OrderPaidEvent.OrderItemPayload> items = order.getItems().stream()
                .map(item -> new OrderPaidEvent.OrderItemPayload(item.getProductId(), item.getQuantity()))
                .toList();

        orderEventPublisher.publishOrderPaid(new OrderPaidEvent(order.getOrderId(), items));
    }
}
