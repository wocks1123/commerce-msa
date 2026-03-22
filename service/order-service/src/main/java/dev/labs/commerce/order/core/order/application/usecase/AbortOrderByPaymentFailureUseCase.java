package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.event.OrderEventPublisher;
import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByPaymentFailureCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import dev.labs.commerce.order.core.order.application.event.OrderAbortedEvent;
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
public class AbortOrderByPaymentFailureUseCase {

    private final SalesOrderRepository salesOrderRepository;
    private final OrderEventPublisher orderEventPublisher;

    public void execute(AbortOrderByPaymentFailureCommand command) {
        SalesOrder order = salesOrderRepository.findByIdWithLock(command.orderId())
                .orElseThrow(OrderNotFoundException::new);

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in {} state, skipping abort by payment failure",
                    command.orderId(), order.getStatus());
            return;
        }

        order.abort(Instant.now());

        List<OrderAbortedEvent.OrderItemPayload> itemPayloads = order.getItems().stream()
                .map(item -> new OrderAbortedEvent.OrderItemPayload(
                        item.getProductId(),
                        item.getQuantity()
                ))
                .toList();

        orderEventPublisher.publishOrderAborted(new OrderAbortedEvent(
                order.getOrderId(),
                itemPayloads
        ));
    }
}
