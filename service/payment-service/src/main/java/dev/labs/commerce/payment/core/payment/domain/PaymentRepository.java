package dev.labs.commerce.payment.core.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String>, PaymentCustomRepository {

    Optional<Payment> findByOrderId(String orderId);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByOrderId(String orderId);

}
