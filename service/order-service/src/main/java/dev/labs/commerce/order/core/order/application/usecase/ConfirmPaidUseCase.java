package dev.labs.commerce.order.core.order.application.usecase;

import dev.labs.commerce.order.core.order.application.usecase.dto.ConfirmPaidCommand;
import dev.labs.commerce.order.core.order.domain.SalesOrder;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import dev.labs.commerce.order.core.order.domain.error.OrderErrorCode;
import dev.labs.commerce.order.core.order.domain.error.OrderNotFoundException;
import dev.labs.commerce.order.core.order.domain.error.OrderPaymentMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class ConfirmPaidUseCase {

    private final SalesOrderRepository salesOrderRepository;

    public void execute(ConfirmPaidCommand command) {
        SalesOrder order = salesOrderRepository.findById(command.orderId())
                .orElseThrow(OrderNotFoundException::new);

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
    }
}
