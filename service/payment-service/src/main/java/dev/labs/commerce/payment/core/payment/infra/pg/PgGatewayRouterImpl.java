package dev.labs.commerce.payment.core.payment.infra.pg;

import dev.labs.commerce.payment.core.payment.application.port.PgGateway;
import dev.labs.commerce.payment.core.payment.application.port.PgGatewayRouter;
import dev.labs.commerce.payment.core.payment.domain.PgProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
class PgGatewayRouterImpl implements PgGatewayRouter {

    private final Map<String, PgGateway> gateways;


    @Override
    public PgGateway route(PgProvider pgProvider) {
        return gateways.get(pgProvider.name());
    }

}
