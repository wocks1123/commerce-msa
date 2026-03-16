package dev.labs.commerce.payment.api.http;

import dev.labs.commerce.common.web.doc.ApiBadRequestResponse;
import dev.labs.commerce.common.web.doc.ApiConflictResponse;
import dev.labs.commerce.payment.api.http.dto.InitializePaymentRequest;
import dev.labs.commerce.payment.api.http.dto.InitializePaymentResponse;
import dev.labs.commerce.payment.core.payment.application.usecase.InitializePaymentUseCase;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payment", description = "Payment API")
public class PaymentRestController {

    private final InitializePaymentUseCase initializePaymentUseCase;

    @Operation(summary = "Initialize payment")
    @ApiResponse(responseCode = "201", description = "Payment initialized successfully", content = @Content(schema = @Schema(implementation = InitializePaymentResponse.class)))
    @ApiBadRequestResponse
    @ApiConflictResponse
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InitializePaymentResponse initializePayment(@RequestBody @Valid InitializePaymentRequest request) {
        InitializePaymentCommand command = new InitializePaymentCommand(
                request.orderId(),
                request.customerId(),
                request.amount(),
                request.currency(),
                request.idempotencyKey(),
                PgProvider.MOCK_PAY,
                Instant.now(),
                request.items().stream()
                        .map(i -> new InitializePaymentCommand.Item(i.productId(), i.quantity()))
                        .toList()
        );

        InitializePaymentResult result = initializePaymentUseCase.execute(command);

        return new InitializePaymentResponse(
                result.paymentId(),
                result.orderId(),
                result.status(),
                result.amount(),
                result.currency(),
                result.requestedAt()
        );
    }

}
