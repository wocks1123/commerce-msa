package dev.labs.commerce.order.api.scheduling;

import dev.labs.commerce.order.config.OrderExpiryProperties;
import dev.labs.commerce.order.core.order.application.usecase.ExpireOrderUseCase;
import dev.labs.commerce.order.core.order.application.usecase.dto.ExpireOrderCommand;
import dev.labs.commerce.order.core.order.domain.OrderStatus;
import dev.labs.commerce.order.core.order.domain.SalesOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderExpiryScheduler {

    private final SalesOrderRepository salesOrderRepository;
    private final ExpireOrderUseCase expireOrderUseCase;
    private final OrderExpiryProperties properties;


    @Scheduled(fixedDelay = 60_000)
    public void expireOrders() {
        Instant threshold = Instant.now().minus(properties.getPendingExpiryMinutes(), ChronoUnit.MINUTES);
        List<String> orderIds = salesOrderRepository.findOrderIdsByStatusAndPendingAtBefore(OrderStatus.CREATED, threshold);

        if (orderIds.isEmpty()) {
            return;
        }

        log.info("Expiring {} orders older than {} minutes", orderIds.size(), properties.getPendingExpiryMinutes());
        orderIds.forEach(orderId -> {
            try {
                expireOrderUseCase.execute(new ExpireOrderCommand(orderId));
            } catch (Exception e) {
                log.error("Failed to expire order: orderId={}", orderId, e);
            }
        });
    }
}
