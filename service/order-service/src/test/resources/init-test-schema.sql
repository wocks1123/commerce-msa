CREATE SCHEMA IF NOT EXISTS "order";

CREATE TABLE IF NOT EXISTS outbox_message
(
    id            BIGSERIAL    PRIMARY KEY,
    event_id      UUID         NOT NULL UNIQUE,
    event_type    VARCHAR(100) NOT NULL,
    destination   VARCHAR(100) NOT NULL,
    partition_key VARCHAR(64)  NOT NULL,
    payload       JSONB        NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMPTZ  NOT NULL,
    processed_at  TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_outbox_message_id
    ON outbox_message (id) WHERE status = 'PENDING';
