package dev.labs.commerce.payment.core.payment.application.port;

import dev.labs.commerce.payment.core.payment.domain.PgProvider;

public interface PgGatewayRouter {

    PgGateway route(PgProvider pgProvider);

}
