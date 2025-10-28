CREATE SEQUENCE IF NOT EXISTS car_models_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS maintenances_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS vehicles_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE car_models
(
    id                BIGINT           NOT NULL,
    version           BIGINT           NOT NULL,
    brand             VARCHAR(255)     NOT NULL,
    model             VARCHAR(255)     NOT NULL,
    year              VARCHAR(4)       NOT NULL,
    segment           VARCHAR(255)     NOT NULL,
    category          VARCHAR(255)     NOT NULL,
    engine_type       VARCHAR(255)     NOT NULL,
    transmission_type VARCHAR(255)     NOT NULL,
    drivetrain        VARCHAR(255)     NOT NULL,
    rental_price      DOUBLE PRECISION NOT NULL,
    CONSTRAINT pk_car_models PRIMARY KEY (id)
);

CREATE TABLE maintenances
(
    id                           BIGINT                      NOT NULL,
    version                      BIGINT                      NOT NULL,
    type                         VARCHAR(255),
    start_date                   TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_end_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_end_date              TIMESTAMP WITHOUT TIME ZONE,
    start_fleet_manager_username VARCHAR(255)                NOT NULL,
    end_fleet_manager_username   VARCHAR(255),
    CONSTRAINT pk_maintenances PRIMARY KEY (id)
);

CREATE TABLE reservations
(
    id                             BIGINT                      NOT NULL,
    version                        BIGINT                      NOT NULL,
    customer_username              VARCHAR(255)                NOT NULL,
    creation_date                  TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_pick_up_date           TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_pick_up_date            TIMESTAMP WITHOUT TIME ZONE,
    planned_drop_off_date          TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_drop_off_date           TIMESTAMP WITHOUT TIME ZONE,
    buffered_drop_off_date         TIMESTAMP WITHOUT TIME ZONE,
    status                         VARCHAR(255)                NOT NULL,
    total_amount                   DOUBLE PRECISION            NOT NULL,
    was_delivery_late              BOOLEAN,
    was_charged_fee                BOOLEAN,
    was_involved_in_accident       BOOLEAN,
    damage_level                   INTEGER,
    dirtiness_level                INTEGER,
    pick_up_staff_username         VARCHAR(255),
    drop_off_staff_username        VARCHAR(255),
    updated_vehicle_staff_username VARCHAR(255),
    CONSTRAINT pk_reservations PRIMARY KEY (id)
);

CREATE TABLE vehicles
(
    id               BIGINT           NOT NULL,
    version          BIGINT           NOT NULL,
    entry_date       date             NOT NULL,
    license_plate    VARCHAR(7)       NOT NULL,
    vin              VARCHAR(17)      NOT NULL,
    status           VARCHAR(255)     NOT NULL,
    km_travelled     DOUBLE PRECISION NOT NULL,
    pending_cleaning BOOLEAN          NOT NULL,
    CONSTRAINT pk_vehicles PRIMARY KEY (id)
);

ALTER TABLE car_models
    ADD CONSTRAINT uc_a60c57994c6e61272374723e9 UNIQUE (brand, model, year);

ALTER TABLE vehicles
    ADD CONSTRAINT uc_ce84d66695b7d27695f954bff UNIQUE (entry_date, license_plate);

ALTER TABLE vehicles
    ADD CONSTRAINT uc_dbc928eecab48d47c0c5bbdcb UNIQUE (entry_date, vin);

-- Car models
INSERT INTO car_models (id, version, brand, model, year, segment, category,
                        engine_type, transmission_type, drivetrain, rental_price)
VALUES (1, 1, 'Toyota', 'Corolla', '2022', 'COMPACT', 'ECONOMY', 'PETROL', 'AUTOMATIC', 'FWD', 40.00),

       (2, 1, 'Honda', 'Civic', '2023', 'COMPACT', 'MIDSIZE', 'PETROL', 'MANUAL', 'FWD', 38.50),

       (3, 1, 'Ford', 'Mustang', '2021', 'SPORT', 'LUXURY', 'PETROL', 'AUTOMATIC', 'RWD', 100.00),

       (4, 1, 'Tesla', 'Model 3', '2023', 'SEDAN', 'FULLSIZE', 'ELECTRIC', 'AUTOMATIC', 'AWD', 85.00),

       (5, 1, 'BMW', 'X5', '2022', 'SUV', 'LUXURY', 'DIESEL', 'AUTOMATIC', 'AWD', 120.00),

       (6, 1, 'Jeep', 'Wrangler', '2022', 'SUV', 'PREMIUM', 'PETROL', 'MANUAL', 'FWD', 90.00),

       (7, 1, 'Hyundai', 'Tucson', '2023', 'SUV', 'FULLSIZE', 'HYBRID', 'AUTOMATIC', 'AWD', 70.00),

       (8, 1, 'Audi', 'A4', '2022', 'SEDAN', 'LUXURY', 'PETROL', 'AUTOMATIC', 'AWD', 95.00),

       (9, 1, 'Mercedes-Benz', 'C-Class', '2023', 'SEDAN', 'PREMIUM', 'PETROL', 'AUTOMATIC', 'RWD', 110.00),

       (10, 1, 'Volkswagen', 'Golf', '2023', 'COMPACT', 'MIDSIZE', 'PETROL', 'MANUAL', 'FWD', 35.00);
SELECT setval('car_models_seq', (SELECT MAX(id) FROM car_models));

-- Vehicles (15)
INSERT INTO vehicles (id, version, entry_date, license_plate, vin, status, km_travelled, pending_cleaning)
VALUES
-- First day
-- Vehicles for Car Model ID 1
(1, 1, '2025-04-11', 'ABC1234', '1HGCM82633A000001', 'RENTED', 25, FALSE),
(2, 1, '2025-04-11', 'XYZ5678', '2T1BURHE9JC000002', 'RENTED', 60, FALSE),

-- Vehicles for Car Model ID 2
(3, 1, '2025-04-11', 'JKL9012', '3VWFE21C04M000003', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 3
(4, 1, '2025-04-11', 'DEF3456', 'WVWZZZ3CZ8E000004', 'RENTED', 0, FALSE),
(5, 1, '2025-04-11', 'GHI7890', '1N4AL3AP2DN000005', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 4
(6, 1, '2025-04-11', 'LMN2345', '5NPDH4AE7DH000006', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 5
(7, 1, '2025-04-11', 'PQR6789', '1FAFP34N96W000007', 'AVAILABLE', 0, FALSE),
(8, 1, '2025-04-11', 'STU9876', 'JHMFA16586S000008', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 6
(9, 1, '2025-04-11', 'VWX4321', '2HGES26751H000009', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 7
(10, 1, '2025-04-11', 'YZA8765', '1J4FA39S34P000010', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 8
(11, 1, '2025-04-11', 'BCD5432', '3D7MX48A77G000011', 'AVAILABLE', 0, TRUE),
(12, 1, '2025-04-11', 'EFG2198', 'KMHCT4AE0FU000012', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 9
(13, 1, '2025-04-11', 'HIJ3276', '5XYKT3A1XDG000013', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 10
(14, 1, '2025-04-11', 'KLM7645', '2T3ZF4DV4BW000014', 'AVAILABLE', 0, FALSE),
(15, 1, '2025-04-11', 'NOP4321', '1G1PC5SB1E000015', 'AVAILABLE', 0, FALSE),

-- Second day
-- Vehicles for Car Model ID 1
(16, 1, '2025-04-12', 'ABC1234', '1HGCM82633A000001', 'AVAILABLE', 0, FALSE),
(17, 1, '2025-04-12', 'XYZ5678', '2T1BURHE9JC000002', 'RENTED', 50, FALSE),

-- Vehicles for Car Model ID 2
(18, 1, '2025-04-12', 'JKL9012', '3VWFE21C04M000003', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 3
(19, 1, '2025-04-12', 'DEF3456', 'WVWZZZ3CZ8E000004', 'RENTED', 35, FALSE),
(20, 1, '2025-04-12', 'GHI7890', '1N4AL3AP2DN000005', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 4
(21, 1, '2025-04-12', 'LMN2345', '5NPDH4AE7DH000006', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 5
(22, 1, '2025-04-12', 'PQR6789', '1FAFP34N96W000007', 'AVAILABLE', 0, FALSE),
(23, 1, '2025-04-12', 'STU9876', 'JHMFA16586S000008', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 6
(24, 1, '2025-04-12', 'VWX4321', '2HGES26751H000009', 'AVAILABLE', 0, TRUE),

-- Vehicles for Car Model ID 7
(25, 1, '2025-04-12', 'YZA8765', '1J4FA39S34P000010', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 8
(26, 1, '2025-04-12', 'BCD5432', '3D7MX48A77G000011', 'AVAILABLE', 0, TRUE),
(27, 1, '2025-04-12', 'EFG2198', 'KMHCT4AE0FU000012', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 9
(28, 1, '2025-04-12', 'HIJ3276', '5XYKT3A1XDG000013', 'AVAILABLE', 0, FALSE),

-- Vehicles for Car Model ID 10
(29, 1, '2025-04-12', 'KLM7645', '2T3ZF4DV4BW000014', 'AVAILABLE', 0, FALSE),
(30, 1, '2025-04-12', 'NOP4321', '1G1PC5SB1E000015', 'AVAILABLE', 0, FALSE);
SELECT setval('vehicles_seq', (SELECT MAX(id) FROM vehicles));

-- Maintenance records
INSERT INTO maintenances (id, version, type, start_date, planned_end_date,
                          actual_end_date, start_fleet_manager_username, end_fleet_manager_username)
VALUES (1, 1, 'BRAKES', '2025-04-20 10:15:30'::timestamp, '2025-04-22 10:15:30'::timestamp, '2025-04-22 12:15:30'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (2, 1, 'BATTERY', '2024-03-02 14:30:45'::timestamp, '2024-03-05 16:30:45'::timestamp, '2024-03-05 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (3, 1, 'OIL_CHANGE', '2025-04-22 14:30:45'::timestamp, '2025-04-23 16:30:45'::timestamp, '2025-04-23 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (4, 1, 'ENGINE', '2024-03-04 11:10:00'::timestamp, '2024-03-08 16:30:45'::timestamp, '2024-03-09 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (5, 1, 'CHASSIS', '2024-03-05 16:45:22'::timestamp, '2024-03-06 18:45:22'::timestamp, '2024-03-06 20:45:22'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (6, 1, 'OTHER', '2024-03-06 08:05:55'::timestamp, '2024-03-06 10:05:55'::timestamp, '2024-03-06 12:05:55'::timestamp,
        'fleetmanager', 'fleetmanager'),
       (7, 1, 'TIRES', '2024-03-07 12:30:40'::timestamp,
        '2024-03-08 14:30:40'::timestamp, '2024-03-08 16:30:40'::timestamp, 'fleetmanager', 'fleetmanager');
SELECT setval('maintenances_seq', (SELECT MAX(id) FROM maintenances));

-- Reservation 1: Successful reservation with no issues
INSERT INTO reservations (
    id, version, customer_username, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    buffered_drop_off_date, status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username, updated_vehicle_staff_username
) VALUES (
             1, 1, 'customer1', '2025-04-01 09:00:00',
             '2025-04-03 10:00:00', '2025-04-03 10:00:00',
             '2025-04-10 10:00:00', '2025-04-10 09:45:00',
             '2025-04-11 10:00:00',
             'DELIVERED',
             320.00,
             NULL, FALSE, FALSE, 0, 0, 'staff', 'staff', 'staff'
         );


-- Reservation 2: Late delivery and damage, still completed
INSERT INTO reservations (
    id, version, customer_username, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    buffered_drop_off_date, status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username, updated_vehicle_staff_username
) VALUES (
             2, 1, 'customer2', '2025-04-05 12:00:00',
             '2025-04-06 09:00:00', '2025-04-06 10:30:00',
             '2025-04-12 09:00:00', '2025-04-12 10:00:00',
             '2025-04-15 10:00:00',
             'DELIVERED',
             280.00,
             TRUE, TRUE, TRUE, 2, 1, 'staff', 'staff', 'staff'
         );

-- Reservation 3: Cancelled before pickup
INSERT INTO reservations (
    id, version, customer_username, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    buffered_drop_off_date, status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username, updated_vehicle_staff_username
) VALUES (
             3, 1, 'customer1', '2025-04-07 15:00:00',
             '2025-04-09 08:00:00', NULL,
             '2025-05-15 08:00:00', NULL,
             '2025-05-17 08:00:00',
             'CANCELLED',
             269.50,
             NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL
         );

-- Reservation 4: Accident involved
INSERT INTO reservations (
    id, version, customer_username, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    buffered_drop_off_date, status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username, updated_vehicle_staff_username
) VALUES (
             4, 1, 'customer2', '2025-04-10 11:00:00',
             '2025-04-11 09:00:00', '2025-04-11 09:15:00',
             '2025-04-17 09:00:00', '2025-04-17 10:00:00',
             '2025-04-20 10:00:00',
             'DELIVERED',
             700.00,
             TRUE, TRUE, TRUE, 3, 2, 'staff', 'staff', 'staff'
         );
SELECT setval('reservations_seq', (SELECT MAX(id) FROM reservations));
