CREATE SEQUENCE IF NOT EXISTS tracking_points_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS tracking_sessions_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE tracking_points
(
    id                   BIGINT                      NOT NULL,
    version              BIGINT                      NOT NULL,
    lat                  DOUBLE PRECISION            NOT NULL,
    lng                  DOUBLE PRECISION            NOT NULL,
    timestamp            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    bearing              DOUBLE PRECISION,
    distance_incremental DOUBLE PRECISION,
    tracking_session_id  BIGINT                      NOT NULL,
    CONSTRAINT pk_tracking_points PRIMARY KEY (id)
);

CREATE TABLE tracking_sessions
(
    id                BIGINT                      NOT NULL,
    version           BIGINT                      NOT NULL,
    vehicle_id        BIGINT                      NOT NULL,
    reservation_id    BIGINT                      NOT NULL,
    customer_username VARCHAR(255)                NOT NULL,
    start_date        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    end_date          TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_tracking_sessions PRIMARY KEY (id)
);

ALTER TABLE tracking_points
    ADD CONSTRAINT FK_TRACKING_POINTS_ON_TRACKINGSESSION FOREIGN KEY (tracking_session_id) REFERENCES tracking_sessions (id);