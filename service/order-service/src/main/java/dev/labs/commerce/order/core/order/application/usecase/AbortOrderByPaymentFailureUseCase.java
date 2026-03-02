package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.AbortOrderByPaymentFailureCommand;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class AbortOrderByPaymentFailureUseCase {

    private final SalesOrderRepository salesOrderRepository;

    public void execute(AbortOrderByPaymentFailureCommand command) {
        SalesOrder order = salesOrderRepository.findById(command.orderId())
                .orElseThrow(OrderNotFoundException::new);
        order.abortByPaymentFailure(Instant.now());
    }
}
