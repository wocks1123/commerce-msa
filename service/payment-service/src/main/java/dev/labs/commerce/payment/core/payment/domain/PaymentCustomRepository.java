package dev.labs.commerce.payment.core.payment.domain;

import java.util.Optional;

public interface PaymentCustomRepository {

    Optional<Payment> findByOrderIdWithLock(String orderId);

}
