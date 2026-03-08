package dev.labs.commerce.payment.api.http;

import dev.labs.commerce.payment.api.http.dto.MockPayFailResponse;
import dev.labs.commerce.payment.api.http.dto.MockPaySuccessResponse;
import dev.labs.commerce.payment.core.payment.application.usecase.ApprovePaymentUseCase;
import dev.labs.commerce.payment.core.payment.application.usecase.FailPaymentUseCase;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.ApprovePaymentResult;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.FailPaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments/mock-pay")
@RequiredArgsConstructor
public class MockPayCallbackRestController {

    private final ApprovePaymentUseCase approvePaymentUseCase;
    private final FailPaymentUseCase failPaymentUseCase;

    @GetMapping("/success")
    public MockPaySuccessResponse success(@RequestParam String pgTxId,
                                          @RequestParam String orderId,
                                          @RequestParam long amount) {
        ApprovePaymentResult result = approvePaymentUseCase.execute(
                new ApprovePaymentCommand(orderId, pgTxId, amount)
        );

        return new MockPaySuccessResponse(result.paymentId(), result.status().name());
    }

    @GetMapping("/fail")
    public MockPayFailResponse fail(@RequestParam String orderId,
                                    @RequestParam String failureCode,
                                    @RequestParam String failureMessage) {
        FailPaymentResult result = failPaymentUseCase.execute(
                new FailPaymentCommand(orderId, failureCode, failureMessage)
        );

        return new MockPayFailResponse(result.paymentId(), result.status().name());
    }

}
