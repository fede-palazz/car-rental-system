CREATE SEQUENCE IF NOT EXISTS payment_records_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS paypal_outbox_events_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE payment_records
(
    id                BIGINT           NOT NULL,
    version           BIGINT           NOT NULL,
    reservation_id    BIGINT           NOT NULL,
    customer_username VARCHAR(255)     NOT NULL,
    amount            DOUBLE PRECISION NOT NULL,
    token             VARCHAR(255),
    status            VARCHAR(255),
    CONSTRAINT pk_payment_records PRIMARY KEY (id)
);

CREATE TABLE paypal_outbox_events
(
    id             BIGINT       NOT NULL,
    version        BIGINT       NOT NULL,
    payment_id     BIGINT       NOT NULL,
    paypal_token   VARCHAR(255) NOT NULL,
    payer_id       VARCHAR(255) NOT NULL,
    reservation_id BIGINT       NOT NULL,
    created_at     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_paypal_outbox_events PRIMARY KEY (id)
);