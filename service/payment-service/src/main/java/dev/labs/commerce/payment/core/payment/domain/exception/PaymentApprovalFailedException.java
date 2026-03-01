package dev.labs.commerce.payment.core.payment.domain.exception;

import dev.labs.commerce.common.error.ValidationException;
import org.jspecify.annotations.Nullable;

public class PaymentApprovalFailedException extends ValidationException {

    public PaymentApprovalFailedException(String failureCode, @Nullable String failureMessage) {
        super(PaymentErrorCode.PAYMENT_APPROVAL_FAILED,
                "pgFailureCode=" + failureCode + (failureMessage != null ? ", " + failureMessage : ""));
    }
}
