package dev.labs.commerce.product.api.scheduling;

import dev.labs.commerce.product.config.ProductActivationProperties;
import dev.labs.commerce.product.core.product.application.usecase.ActivateScheduledProductUseCase;
import dev.labs.commerce.product.core.product.application.usecase.dto.ActivateScheduledProductCommand;
import dev.labs.commerce.product.core.product.domain.ProductDao;
import dev.labs.commerce.product.core.product.domain.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductActivationScheduler {

    private final ProductDao productDao;
    private final ActivateScheduledProductUseCase activateScheduledProductUseCase;
    private final ProductActivationProperties properties;

    @Scheduled(fixedDelay = 60_000)
    public void activateScheduledProducts() {
        Instant now = Instant.now();
        List<Long> productIds = productDao.findIdsByStatusInSalePeriod(
                ProductStatus.INACTIVE, now, properties.getBatchSize()
        );

        if (productIds.isEmpty()) {
            return;
        }

        log.info("Activating {} products whose sale period has started", productIds.size());
        productIds.forEach(productId -> {
            try {
                activateScheduledProductUseCase.execute(new ActivateScheduledProductCommand(productId));
            } catch (Exception e) {
                log.error("Failed to activate product: productId={}", productId, e);
            }
        });
    }
}
