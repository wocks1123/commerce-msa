CREATE TABLE IF NOT EXISTS payment
(
    payment_id      VARCHAR(50) PRIMARY KEY,
    order_id        VARCHAR(36) NOT NULL,
    customer_id     BIGINT      NOT NULL,
    status          VARCHAR(20) NOT NULL,
    amount          BIGINT      NOT NULL,
    currency        VARCHAR(3)  NOT NULL,
    idempotency_key VARCHAR(80) NOT NULL,
    pg_provider     VARCHAR(30) NOT NULL,
    pg_tx_id        VARCHAR(100),
    failure_code    VARCHAR(50),
    failure_message VARCHAR(500),
    requested_at    TIMESTAMPTZ NOT NULL,
    in_progress_at  TIMESTAMPTZ,
    approved_at     TIMESTAMPTZ,
    failed_at       TIMESTAMPTZ,
    canceled_at     TIMESTAMPTZ,
    version         BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL,
    updated_at      TIMESTAMPTZ NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_order_id
    ON payment (order_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_payment_idempotency_key
    ON payment (idempotency_key);

CREATE INDEX IF NOT EXISTS idx_payment_customer_id_created_at
    ON payment (customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_payment_status_created_at
    ON payment (status, created_at DESC);
