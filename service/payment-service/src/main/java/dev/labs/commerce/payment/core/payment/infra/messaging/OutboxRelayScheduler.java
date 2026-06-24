package dev.labs.commerce.payment.core.payment.infra.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * OutboxRelay를 주기적으로 트리거
 */
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxRelay outboxRelay;

    @Scheduled(fixedDelay = 1000, scheduler = "outboxRelayTaskScheduler")
    public void dispatch() {
        outboxRelay.dispatchPending();
    }

}
