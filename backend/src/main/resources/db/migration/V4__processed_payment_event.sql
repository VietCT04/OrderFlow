CREATE TABLE processed_payment_event (
    payment_id   UUID        PRIMARY KEY,
    event_type   VARCHAR(64) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_processed_payment_event_processed_at
    ON processed_payment_event (processed_at);
