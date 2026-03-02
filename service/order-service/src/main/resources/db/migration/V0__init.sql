CREATE TABLE IF NOT EXISTS sales_order
(
    order_id           VARCHAR(36) PRIMARY KEY,
    customer_id        BIGINT      NOT NULL,
    status             VARCHAR(20) NOT NULL,
    total_price        BIGINT      NOT NULL,
    total_amount       BIGINT      NOT NULL,
    currency           VARCHAR(3)  NOT NULL,
    version            BIGINT      NOT NULL,
    payment_pending_at TIMESTAMPTZ,
    paid_at            TIMESTAMPTZ,
    aborted_at         TIMESTAMPTZ,
    cancelled_at       TIMESTAMPTZ,
    failed_at          TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_sales_order_customer_id_created_at
    ON sales_order (customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_sales_order_status_created_at
    ON sales_order (status, created_at DESC);

CREATE TABLE IF NOT EXISTS order_item
(
    order_item_id BIGSERIAL PRIMARY KEY,
    order_id      VARCHAR(36)  NOT NULL,
    product_id    BIGINT       NOT NULL,
    product_name  VARCHAR(200) NOT NULL,
    unit_price    BIGINT       NOT NULL,
    quantity      INTEGER      NOT NULL,
    line_amount   BIGINT       NOT NULL,
    currency      VARCHAR(3)   NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    updated_at    TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_order_item_order_id
    ON order_item (order_id);

CREATE INDEX IF NOT EXISTS idx_order_item_product_id
    ON order_item (product_id);
