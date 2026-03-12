package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmStockReservedCommand;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class ConfirmStockReservedUseCase {

    private final SalesOrderRepository salesOrderRepository;

    public void execute(ConfirmStockReservedCommand command) {
        SalesOrder order = salesOrderRepository.findById(command.orderId())
                .orElseThrow(OrderNotFoundException::new);
        order.confirmStockReserved(Instant.now());
    }
}
