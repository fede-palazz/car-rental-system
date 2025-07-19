-- Create initial schema

-- V1__InitialVersion.sql
CREATE SEQUENCE IF NOT EXISTS car_features_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS car_models_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS maintenances_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS notes_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS vehicles_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE car_features
(
    id          BIGINT       NOT NULL,
    description VARCHAR(255) NOT NULL,
    CONSTRAINT pk_car_features PRIMARY KEY (id)
);

CREATE TABLE car_models
(
    id                 BIGINT           NOT NULL,
    brand              VARCHAR(255)     NOT NULL,
    model              VARCHAR(255)     NOT NULL,
    year               VARCHAR(4)       NOT NULL,
    segment            VARCHAR(255)     NOT NULL,
    doors_number       INTEGER          NOT NULL,
    seating_capacity   INTEGER          NOT NULL,
    luggage_capacity   DOUBLE PRECISION NOT NULL,
    category           VARCHAR(255)     NOT NULL,
    engine_type        VARCHAR(255)     NOT NULL,
    transmission_type  VARCHAR(255)     NOT NULL,
    drivetrain         VARCHAR(255)     NOT NULL,
    motor_displacement INTEGER,
    rental_price       DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_car_models PRIMARY KEY (id)
);

CREATE TABLE car_models_features
(
    car_model_id BIGINT NOT NULL,
    feature_id   BIGINT NOT NULL
);

CREATE TABLE maintenances
(
    id                     BIGINT                      NOT NULL,
    defects                VARCHAR(255)                NOT NULL,
    completed              BOOLEAN                     NOT NULL,
    type                   VARCHAR(255),
    upcoming_service_needs VARCHAR(255)                NOT NULL,
    date                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vehicle_id             BIGINT                      NOT NULL,
    CONSTRAINT pk_maintenances PRIMARY KEY (id)
);

CREATE TABLE notes
(
    id         BIGINT                      NOT NULL,
    content    TEXT                        NOT NULL,
    author     VARCHAR(255)                NOT NULL,
    date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    vehicle_id BIGINT                      NOT NULL,
    CONSTRAINT pk_notes PRIMARY KEY (id)
);



CREATE TABLE vehicles
(
    id               BIGINT           NOT NULL,
    license_plate    VARCHAR(7)       NOT NULL,
    vin              VARCHAR(17)      NOT NULL,
    car_model_id     BIGINT           NOT NULL,
    status           VARCHAR(255)     NOT NULL,
    km_travelled     DOUBLE PRECISION NOT NULL,
    pending_cleaning BOOLEAN          NOT NULL,
    pending_repair   BOOLEAN          NOT NULL,
    CONSTRAINT pk_vehicles PRIMARY KEY (id)
);

ALTER TABLE car_models
    ADD CONSTRAINT uc_05623e301bcbff0e70f6614db UNIQUE (brand, model);

ALTER TABLE car_features
    ADD CONSTRAINT uc_car_features_description UNIQUE (description);

ALTER TABLE vehicles
    ADD CONSTRAINT uc_vehicles_licenseplate UNIQUE (license_plate);

ALTER TABLE vehicles
    ADD CONSTRAINT uc_vehicles_vin UNIQUE (vin);

ALTER TABLE maintenances
    ADD CONSTRAINT FK_MAINTENANCES_ON_VEHICLE FOREIGN KEY (vehicle_id) REFERENCES vehicles (id);

ALTER TABLE notes
    ADD CONSTRAINT FK_NOTES_ON_VEHICLE FOREIGN KEY (vehicle_id) REFERENCES vehicles (id);

ALTER TABLE vehicles
    ADD CONSTRAINT FK_VEHICLES_ON_CARMODEL FOREIGN KEY (car_model_id) REFERENCES car_models (id);

ALTER TABLE car_models_features
    ADD CONSTRAINT fk_carmodfea_on_car_feature FOREIGN KEY (feature_id) REFERENCES car_features (id);

ALTER TABLE car_models_features
    ADD CONSTRAINT fk_carmodfea_on_car_model FOREIGN KEY (car_model_id) REFERENCES car_models (id);

-- V2__AddedReservationTable.sql
CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE reservations
(
    id                       BIGINT                      NOT NULL,
    customer_id              BIGINT                      NOT NULL,
    vehicle_id               BIGINT                      NOT NULL,
    creation_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_pick_up_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_pick_up_date      TIMESTAMP WITHOUT TIME ZONE,
    planned_drop_off_date    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_drop_off_date     TIMESTAMP WITHOUT TIME ZONE,
    status                   VARCHAR(255)                NOT NULL,
    was_delivery_late        BOOLEAN,
    was_charged_fee          BOOLEAN,
    was_vehicle_damaged      BOOLEAN,
    was_involved_in_accident BOOLEAN,
    CONSTRAINT pk_reservations PRIMARY KEY (id)
);

ALTER TABLE reservations
    ADD CONSTRAINT FK_RESERVATIONS_ON_VEHICLE FOREIGN KEY (vehicle_id) REFERENCES vehicles (id);

-- V3__AddedVersionColumn.sql
ALTER TABLE car_features ADD COLUMN version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE car_models ADD COLUMN version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE maintenances ADD COLUMN version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE notes ADD COLUMN version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE reservations ADD COLUMN version BIGINT NOT NULL DEFAULT 1;

ALTER TABLE vehicles ADD COLUMN version BIGINT NOT NULL DEFAULT 1;
