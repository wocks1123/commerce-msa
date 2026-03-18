package dev.labs.commerce.payment.core.payment.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.labs.commerce.payment.core.payment.domain.Payment;
import dev.labs.commerce.payment.core.payment.domain.PaymentCustomRepository;
import dev.labs.commerce.payment.core.payment.domain.QPayment;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentCustomRepository {

    private final JPAQueryFactory factory;

    @Override
    public Optional<Payment> findByOrderIdWithLock(String orderId) {
        return Optional.ofNullable(
                factory.selectFrom(QPayment.payment)
                        .where(QPayment.payment.orderId.eq(orderId))
                        .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                        .fetchOne()
        );
    }

}
