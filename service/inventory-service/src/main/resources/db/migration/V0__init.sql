CREATE TABLE IF NOT EXISTS inventory
(
    product_id        BIGINT PRIMARY KEY,
    total_quantity    INTEGER     NOT NULL,
    reserved_quantity INTEGER     NOT NULL,
    version           BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);

CREATE TABLE inventory_history
(
    inventory_history_id BIGSERIAL PRIMARY KEY,
    order_id             VARCHAR(36),
    product_id           BIGINT      NOT NULL,
    operation_type       VARCHAR(20) NOT NULL,
    quantity             INT         NOT NULL,
    total_after          INT         NOT NULL,
    reserved_after       INT         NOT NULL,
    actor                VARCHAR(20) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL,
    updated_at           TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_inventory_history_product_id_created_at
    ON inventory_history (product_id, created_at DESC);

CREATE UNIQUE INDEX IF NOT EXISTS uk_inventory_history_order_product_operation
    ON inventory_history (order_id, product_id, operation_type)
    WHERE order_id IS NOT NULL;
