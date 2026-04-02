package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByStockFailureCommand;
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
public class AbortOrderByStockFailureUseCase {

    private final SalesOrderRepository salesOrderRepository;

    public void execute(AbortOrderByStockFailureCommand command) {
        SalesOrder order = salesOrderRepository.findByIdWithLock(command.orderId())
                .orElseThrow(OrderNotFoundException::new);

        if (order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PENDING) {
            log.warn("Order {} already in {} state, skipping abort by stock failure",
                    command.orderId(), order.getStatus());
            return;
        }

        order.abort(Instant.now());
    }
}
