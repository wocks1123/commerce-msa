package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.InventoryPort;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentRepository;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InitializePaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final InventoryPort inventoryPort;

    public InitializePaymentResult execute(InitializePaymentCommand command) {
        log.info("Initializing payment: orderId={}, amount={}, currency={}",
                command.orderId(), command.amount(), command.currency());

        if (paymentRepository.existsByOrderId(command.orderId())) {
            throw new PaymentAlreadyExistsException("orderId=" + command.orderId());
        }
        if (paymentRepository.findByIdempotencyKey(command.idempotencyKey()).isPresent()) {
            throw new PaymentAlreadyExistsException("idempotencyKey=" + command.idempotencyKey());
        }

        List<InventoryPort.Item> inventoryItems = command.items().stream()
                .map(i -> new InventoryPort.Item(i.productId(), i.quantity()))
                .toList();
        inventoryPort.reserve(command.orderId(), inventoryItems);

        Payment payment = Payment.create(
                command.orderId(),
                command.customerId(),
                command.amount(),
                command.currency(),
                command.idempotencyKey(),
                command.pgProvider(),
                command.requestedAt()
        );

        Payment saved = paymentRepository.save(payment);

        log.info("Payment initialized: paymentId={}, orderId={}, status={}",
                saved.getPaymentId(), saved.getOrderId(), saved.getStatus());

        return new InitializePaymentResult(
                saved.getPaymentId(),
                saved.getOrderId(),
                saved.getStatus(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getRequestedAt()
        );
    }
}
