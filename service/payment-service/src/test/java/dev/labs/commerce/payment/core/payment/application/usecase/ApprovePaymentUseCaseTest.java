package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.command.FinalizePaymentCommandService;
import dev.labs.commerce.payment.core.payment.application.command.FinalizePaymentResult;
import dev.labs.commerce.payment.core.payment.application.command.PreparePaymentCommandService;
import dev.labs.commerce.payment.core.payment.application.command.PreparePaymentResult;
import dev.labs.commerce.payment.core.payment.application.command.ProceedPaymentResult;
import dev.labs.commerce.payment.core.payment.application.command.SkipPaymentResult;
import dev.labs.commerce.payment.core.payment.application.port.PgGateway;
import dev.labs.commerce.payment.core.payment.application.port.PgGatewayRouter;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalCommand;
import dev.labs.commerce.payment.core.payment.application.port.dto.PgApprovalResult;
import dev.labs.commerce.payment.core.payment.application.port.pg.DeclineReason;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgDeclinedException;
import dev.labs.commerce.payment.core.payment.application.port.pg.PgUncertainException;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class ApprovePaymentUseCaseTest {

    @Mock
    private PreparePaymentCommandService prepareService;

    @Mock
    private FinalizePaymentCommandService finalizeService;

    @Mock
    private PgGatewayRouter pgGatewayRouter;

    @Mock
    private PgGateway pgGateway;

    @InjectMocks
    private ApprovePaymentUseCase approvePaymentUseCase;

    @Test
    @DisplayName("Prepare가 Skip을 반환하면 PG 호출과 Finalize 없이 Skip 정보를 그대로 반환한다")
    void execute_whenPrepareSkips_returnsSkipResultWithoutPgCall() {
        // given
        final ApprovePaymentCommand command = new ApprovePaymentCommand("order-1", "pk-1", 10000L);
        final PreparePaymentResult skip = new SkipPaymentResult(
                "p-1", "order-1", PaymentStatus.APPROVED, 10000L, "KRW",
                "pg-tx-1", Instant.now(), null, null
        );
        given(prepareService.prepare(command)).willReturn(skip);

        // when
        final ApprovePaymentResult actual = approvePaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(actual.paymentId()).isEqualTo("p-1");
        then(pgGatewayRouter).shouldHaveNoInteractions();
        then(finalizeService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Prepare가 Proceed를 반환하면 PG로 승인 요청 후 Finalize 결과를 반환한다")
    void execute_whenPrepareProceeds_callsPgThenFinalize() {
        // given
        final ApprovePaymentCommand command = new ApprovePaymentCommand("order-1", "pk-1", 10000L);
        final ProceedPaymentResult proceed = new ProceedPaymentResult("order-1", 100L, 10000L, PgProvider.MOCK_PAY);
        given(prepareService.prepare(command)).willReturn(proceed);
        given(pgGatewayRouter.route(PgProvider.MOCK_PAY)).willReturn(pgGateway);
        final PgApprovalResult pgResult = PgApprovalResult.success("pg-tx-1", 10000L, Instant.now());
        given(pgGateway.approve(any(PgApprovalCommand.class))).willReturn(pgResult);
        final FinalizePaymentResult finalized = new FinalizePaymentResult(
                "p-1", "order-1", PaymentStatus.APPROVED, 10000L, "KRW",
                "pg-tx-1", Instant.now(), null, null
        );
        given(finalizeService.finalize("order-1", pgResult)).willReturn(finalized);

        // when
        final ApprovePaymentResult actual = approvePaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.APPROVED);
        final ArgumentCaptor<PgApprovalCommand> captor = ArgumentCaptor.forClass(PgApprovalCommand.class);
        then(pgGateway).should().approve(captor.capture());
        assertThat(captor.getValue().pgTxId()).isEqualTo("pk-1");
        assertThat(captor.getValue().orderId()).isEqualTo("order-1");
        assertThat(captor.getValue().amount()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("PG가 PgDeclinedException을 던지면 failure() 결과로 Finalize를 호출한다")
    void execute_whenPgDeclined_finalizesWithFailure() {
        // given
        final ApprovePaymentCommand command = new ApprovePaymentCommand("order-1", "pk-1", 10000L);
        final ProceedPaymentResult proceed = new ProceedPaymentResult("order-1", 100L, 10000L, PgProvider.MOCK_PAY);
        given(prepareService.prepare(command)).willReturn(proceed);
        given(pgGatewayRouter.route(PgProvider.MOCK_PAY)).willReturn(pgGateway);
        given(pgGateway.approve(any(PgApprovalCommand.class)))
                .willThrow(new PgDeclinedException(DeclineReason.INSUFFICIENT_BALANCE, "PG_001", "잔액 부족"));
        final FinalizePaymentResult finalized = new FinalizePaymentResult(
                "p-1", "order-1", PaymentStatus.FAILED, 10000L, "KRW",
                null, null, "INSUFFICIENT_BALANCE", "잔액 부족"
        );
        given(finalizeService.finalize(anyString(), any(PgApprovalResult.class))).willReturn(finalized);

        // when
        final ApprovePaymentResult actual = approvePaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.FAILED);
        final ArgumentCaptor<PgApprovalResult> captor = ArgumentCaptor.forClass(PgApprovalResult.class);
        then(finalizeService).should().finalize(eq("order-1"), captor.capture());
        assertThat(captor.getValue().success()).isFalse();
        assertThat(captor.getValue().aborted()).isFalse();
        assertThat(captor.getValue().failureCode()).isEqualTo(DeclineReason.INSUFFICIENT_BALANCE.name());
    }

    @Test
    @DisplayName("PG가 PgUncertainException을 던지면 ofAborted() 결과로 Finalize를 호출한다")
    void execute_whenPgUncertain_finalizesWithAborted() {
        // given
        final ApprovePaymentCommand command = new ApprovePaymentCommand("order-1", "pk-1", 10000L);
        final ProceedPaymentResult proceed = new ProceedPaymentResult("order-1", 100L, 10000L, PgProvider.MOCK_PAY);
        given(prepareService.prepare(command)).willReturn(proceed);
        given(pgGatewayRouter.route(PgProvider.MOCK_PAY)).willReturn(pgGateway);
        given(pgGateway.approve(any(PgApprovalCommand.class)))
                .willThrow(new PgUncertainException("PG_TIMEOUT", "응답 지연", true));
        final FinalizePaymentResult finalized = new FinalizePaymentResult(
                "p-1", "order-1", PaymentStatus.ABORTED, 10000L, "KRW",
                null, null, "PG_TIMEOUT", "응답 지연"
        );
        given(finalizeService.finalize(anyString(), any(PgApprovalResult.class))).willReturn(finalized);

        // when
        final ApprovePaymentResult actual = approvePaymentUseCase.execute(command);

        // then
        assertThat(actual.status()).isEqualTo(PaymentStatus.ABORTED);
        final ArgumentCaptor<PgApprovalResult> captor = ArgumentCaptor.forClass(PgApprovalResult.class);
        then(finalizeService).should().finalize(eq("order-1"), captor.capture());
        assertThat(captor.getValue().aborted()).isTrue();
        assertThat(captor.getValue().failureCode()).isEqualTo("PG_TIMEOUT");
        then(pgGatewayRouter).should().route(PgProvider.MOCK_PAY);
    }
}
