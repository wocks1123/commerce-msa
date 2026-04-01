package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.event.OrderExpiredEvent;
import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.usecase.dto.ExpireOrderCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ExpireOrderUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public void execute(ExpireOrderCommand command) {
        SalesOrder order = salesOrderRepository.findByIdWithLock(command.orderId())
                .orElseThrow(OrderNotFoundException::new);

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in {} state, skipping expiry", command.orderId(), order.getStatus());
            return;
        }

        order.markAsExpired(Instant.now());

        orderEventPublisher.publishOrderExpired(new OrderExpiredEvent(
                order.getOrderId(),
                order.getItems().stream()
                        .map(item -> new OrderExpiredEvent.OrderItemPayload(item.getProductId(), item.getQuantity()))
                        .toList()
        ));
        log.info("Order expired: orderId={}", order.getOrderId());
    }

}
