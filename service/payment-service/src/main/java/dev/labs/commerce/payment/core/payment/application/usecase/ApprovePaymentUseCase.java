package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.command.*;
import dev.labs.commerce.payment.core.payment.application.port.PgGatewayRouter;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgDeclinedException;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgUncertainException;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.NEVER)
@RequiredArgsConstructor
@Slf4j
public class ApprovePaymentUseCase {

    private final PreparePaymentCommandService prepareService;
    private final FinalizePaymentCommandService finalizeService;
    private final PgGatewayRouter pgGatewayRouter;

    public ApprovePaymentResult execute(ApprovePaymentCommand command) {
        PreparePaymentResult prepared = prepareService.prepare(command);
        return switch (prepared) {
            case SkipPaymentResult skip -> ApprovePaymentResult.of(skip);
            case ProceedPaymentResult(String orderId, long customerId, long amount, PgProvider pgProvider) -> {
                PgApprovalResult pgResult;
                try {
                    pgResult = pgGatewayRouter
                            .route(pgProvider)
                            .approve(new PgApprovalCommand(
                                    command.paymentKey(),
                                    orderId,
                                    customerId,
                                    amount
                            ));
                } catch (PgDeclinedException e) {
                    log.warn("PG declined: reason={}, pgCode={}", e.getReason(), e.getPgCode());
                    pgResult = PgApprovalResult.failure(e.getReason().name(), e.getPgMessage());
                } catch (PgUncertainException e) {
                    log.error("PG uncertain: failureCode={}, retryable={}", e.getFailureCode(), e.isRetryable(), e);
                    pgResult = PgApprovalResult.ofAborted(e.getFailureCode(), e.getFailureMessage());
                }
                yield ApprovePaymentResult.of(finalizeService.finalize(orderId, pgResult));
            }
        };
    }

}
