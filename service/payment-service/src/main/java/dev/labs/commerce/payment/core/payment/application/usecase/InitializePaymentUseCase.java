package dev.labs.commerce.payment.core.payment.application.usecase;

import dev.labs.commerce.payment.core.payment.application.event.PaymentEventPublisher;
import dev.labs.commerce.payment.core.payment.application.event.PaymentInitializedEvent;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentCommand;
import dev.labs.commerce.payment.core.payment.application.usecase.dto.InitializePaymentResult;
import dev.labs.commerce.payment.core.payment.domain.*;
import dev.labs.commerce.payment.core.payment.domain.exception.OrderNotPayableException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentAlreadyExistsException;
import dev.labs.commerce.payment.core.payment.domain.exception.PaymentOrderMismatchException;
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
    private final OrderPort orderPort;
    private final InventoryPort inventoryPort;
    private final PaymentEventPublisher paymentEventPublisher;

    public InitializePaymentResult execute(InitializePaymentCommand command) {
        log.info("Initializing payment: orderId={}, amount={}, currency={}",
                command.orderId(), command.amount(), command.currency());

        if (paymentRepository.existsByOrderId(command.orderId())) {
            throw new PaymentAlreadyExistsException("orderId=" + command.orderId());
        }
        if (paymentRepository.findByIdempotencyKey(command.idempotencyKey()).isPresent()) {
            throw new PaymentAlreadyExistsException("idempotencyKey=" + command.idempotencyKey());
        }

        // 주문 원본과 대조한다. 재고 예약보다 앞에 두어 검증 실패 시 점유된 재고가 남지 않게 한다.
        OrderPort.OrderSnapshot order = orderPort.getOrder(command.orderId());
        verifyAgainstOrder(command, order);

        // 예약 품목은 클라이언트 입력이 아니라 주문 원본을 사용한다.
        List<InventoryPort.Item> inventoryItems = order.items().stream()
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

        paymentEventPublisher.publishPaymentInitialized(new PaymentInitializedEvent(
                saved.getPaymentId(),
                saved.getOrderId(),
                saved.getRequestedAt()
        ));

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

    /**
     * 결제 요청 값을 주문 원본과 대조한다.
     */
    private void verifyAgainstOrder(InitializePaymentCommand command, OrderPort.OrderSnapshot order) {
        if (order.status() != OrderStatus.CREATED) {
            throw new OrderNotPayableException(command.orderId(), order.status());
        }
        if (command.customerId() != order.customerId()) {
            throw mismatch(command.orderId(), PaymentOrderMismatchException.Field.CUSTOMER_ID,
                    command.customerId(), order.customerId());
        }
        // 주의: 금액은 totalPrice다. SalesOrder.totalAmount는 수량 합계다.
        if (command.amount() != order.totalPrice()) {
            throw mismatch(command.orderId(), PaymentOrderMismatchException.Field.AMOUNT,
                    command.amount(), order.totalPrice());
        }
        if (!command.currency().equals(order.currency())) {
            throw mismatch(command.orderId(), PaymentOrderMismatchException.Field.CURRENCY,
                    command.currency(), order.currency());
        }
    }

    private PaymentOrderMismatchException mismatch(String orderId,
                                                   PaymentOrderMismatchException.Field field,
                                                   Object requested,
                                                   Object order) {
        log.warn("Payment request does not match order: orderId={}, field={}, requested={}, order={}",
                orderId, field, requested, order);
        return new PaymentOrderMismatchException(field);
    }
}
