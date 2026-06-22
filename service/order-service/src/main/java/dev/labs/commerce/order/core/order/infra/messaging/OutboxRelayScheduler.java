package dev.labs.commerce.order.core.order.infra.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * OutboxRelay를 주기적으로 트리거한다.
 * dispatch 로직의 @Transactional이 적용되도록 relay와 별도 빈으로 둔다(자기호출 시 프록시 미적용 회피).
 */
@Component
@RequiredArgsConstructor
public class OutboxRelayScheduler {

    private final OutboxRelay outboxRelay;

    @Scheduled(fixedDelay = 1000)
    public void dispatch() {
        outboxRelay.dispatchPending();
    }
}
