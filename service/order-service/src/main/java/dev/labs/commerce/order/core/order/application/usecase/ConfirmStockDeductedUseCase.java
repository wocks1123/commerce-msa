package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmStockDeductedCommand;
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
public class ConfirmStockDeductedUseCase {

    private final SalesOrderRepository salesOrderRepository;

    public void execute(ConfirmStockDeductedCommand command) {
        SalesOrder order = salesOrderRepository.findById(command.orderId())
                .orElseThrow(OrderNotFoundException::new);
        order.confirmStockDeducted(Instant.now());
    }
}
