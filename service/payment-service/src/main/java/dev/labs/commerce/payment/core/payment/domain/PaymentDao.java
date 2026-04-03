package dev.labs.commerce.payment.core.payment.domain;

import java.time.Instant;
import java.util.List;

public interface PaymentDao {

    List<String> findIdsByStatusAndRequestedAtBefore(PaymentStatus status, Instant threshold);

}
