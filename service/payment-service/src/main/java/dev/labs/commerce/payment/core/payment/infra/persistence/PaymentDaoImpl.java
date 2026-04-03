package dev.labs.commerce.payment.core.payment.infra.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import dev.labs.commerce.payment.core.payment.domain.PaymentDao;
import dev.labs.commerce.payment.core.payment.domain.PaymentStatus;
import dev.labs.commerce.payment.core.payment.domain.QPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PaymentDaoImpl implements PaymentDao {

    private final JPAQueryFactory factory;

    @Override
    public List<String> findIdsByStatusAndRequestedAtBefore(PaymentStatus status, Instant threshold) {
        return factory.select(QPayment.payment.paymentId)
                .from(QPayment.payment)
                .where(
                        QPayment.payment.status.eq(status),
                        QPayment.payment.requestedAt.before(threshold)
                )
                .fetch();
    }

}
