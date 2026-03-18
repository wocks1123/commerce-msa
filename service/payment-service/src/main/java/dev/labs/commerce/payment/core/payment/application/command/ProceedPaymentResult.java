package dev.labs.commerce.payment.core.payment.application.command;

import dev.labs.commerce.payment.core.payment.domain.PgProvider;

public record ProceedPaymentResult(
        String orderId,
        long customerId,
        long amount,
        PgProvider pgProvider
) implements PreparePaymentResult {
}
