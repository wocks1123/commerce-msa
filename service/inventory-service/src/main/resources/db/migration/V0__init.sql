CREATE TABLE IF NOT EXISTS inventory
(
    product_id        BIGINT PRIMARY KEY,
    total_quantity    INTEGER     NOT NULL,
    reserved_quantity INTEGER     NOT NULL,
    version           BIGINT      NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL
);
