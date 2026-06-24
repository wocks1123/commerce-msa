package dev.labs.commerce.payment.core.payment.infra.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * OutboxRelay를 주기적으로 트리거
 */
@Component
@ConditionalOnProperty(name = "outbox.relay.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxRelay outboxRelay;

    @Scheduled(fixedDelay = 1000, scheduler = "outboxRelayTaskScheduler")
    public void dispatch() {
        outboxRelay.dispatchPending();
    }

}
