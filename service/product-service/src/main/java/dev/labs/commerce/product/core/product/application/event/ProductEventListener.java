package dev.labs.commerce.product.core.product.application.event;

import dev.labs.commerce.product.core.product.domain.event.ProductRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductEventPublisher productEventPublisher;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleProductRegisteredEvent(ProductRegisteredEvent event) {
        productEventPublisher.publishProductRegistered(event);
    }

}
