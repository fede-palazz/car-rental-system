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


INSERT INTO tracking_sessions (
    id, version, vehicle_id, reservation_id, customer_username, start_date, end_date
) VALUES
      --(1, 1, 18, 559, 'customer2', '2025-10-30 16:00:00', NULL),
      --(2, 1, 17, 560, 'customer1', '2025-10-30 16:00:00', NULL),
      (3, 1, 16, 561, 'customer1', '2025-10-30 16:00:00', NULL),
      --(4, 1, 15, 562, 'customer3', '2025-10-30 16:00:00', NULL),
      --(5, 1, 14, 563, 'customer2', '2025-10-30 16:00:00', NULL),
      (6, 1, 13, 564, 'customer3', '2025-10-30 16:00:00', NULL);
SELECT setval('tracking_sessions_seq', (SELECT MAX(id) FROM tracking_sessions));


ALTER TABLE tracking_points
    ADD CONSTRAINT FK_TRACKING_POINTS_ON_TRACKINGSESSION FOREIGN KEY (tracking_session_id) REFERENCES tracking_sessions (id);