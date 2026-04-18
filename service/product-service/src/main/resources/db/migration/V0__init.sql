CREATE TABLE IF NOT EXISTS product
(
    product_id     BIGSERIAL PRIMARY KEY,
    product_name   VARCHAR(200) NOT NULL,
    list_price     BIGINT       NOT NULL,
    selling_price  BIGINT       NOT NULL,
    currency       VARCHAR(3)   NOT NULL,
    product_status VARCHAR(30)  NOT NULL,
    category       VARCHAR(50)  NOT NULL,
    sale_start_at  TIMESTAMPTZ,
    sale_end_at    TIMESTAMPTZ,
    thumbnail_url  VARCHAR(500),
    description    TEXT         NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    updated_at     TIMESTAMPTZ  NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_status ON product (product_status);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON product (created_at);
