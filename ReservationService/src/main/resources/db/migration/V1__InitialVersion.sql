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
    type                   VARCHAR(255),
    upcoming_service_needs VARCHAR(255)                NOT NULL,
    start_date             TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_end_date       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_end_date        TIMESTAMP WITHOUT TIME ZONE,
    start_fleet_manager_username VARCHAR(255),
    end_fleet_manager_username   VARCHAR(255),
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
    ADD CONSTRAINT uc_05623e301bcbff0e70f6614db UNIQUE (brand, model, year);

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


-- ******************
-- ** INITIAL DATA **
-- ******************

-- Car features
INSERT INTO car_features (id, description)
VALUES (1, 'Sunroof'),
       (2, 'Leather Seats'),
       (3, 'Bluetooth'),
       (4, 'Backup Camera'),
       (5, 'Navigation System'),
       (6, 'Heated Seats'),
       (7, 'Keyless Entry'),
       (8, 'Adaptive Cruise Control'),
       (9, 'Blind Spot Monitoring'),
       (10, 'Lane Departure Warning');
SELECT setval('car_features_seq', (SELECT MAX(id) FROM car_features));

-- Car models
INSERT INTO car_models (id, brand, model, year, segment, doors_number, seating_capacity, luggage_capacity, category,
                        engine_type, transmission_type, drivetrain, motor_displacement, rental_price)
VALUES (1, 'Toyota', 'Corolla', '2022', 'COMPACT', 4, 5, 480.0, 'ECONOMY', 'PETROL', 'AUTOMATIC', 'FWD', 1800,
        40.00),

       (2, 'Honda', 'Civic', '2023', 'COMPACT', 4, 5, 450.0, 'MIDSIZE', 'PETROL', 'MANUAL', 'FWD', 1600, 38.50),

       (3, 'Ford', 'Mustang', '2021', 'SPORT', 2, 4, 320.0, 'LUXURY', 'PETROL', 'AUTOMATIC', 'RWD', 5000, 100.00),

       (4, 'Tesla', 'Model 3', '2023', 'SEDAN', 4, 5, 425.0, 'FULLSIZE', 'ELECTRIC', 'AUTOMATIC', 'AWD', NULL,
        85.00),

       (5, 'BMW', 'X5', '2022', 'SUV', 5, 7, 650.0, 'LUXURY', 'DIESEL', 'AUTOMATIC', 'AWD', 3000, 120.00),

       (6, 'Jeep', 'Wrangler', '2022', 'SUV', 4, 5, 700.0, 'PREMIUM', 'PETROL', 'MANUAL', 'FWD', 3600, 90.00),

       (7, 'Hyundai', 'Tucson', '2023', 'SUV', 4, 5, 540.0, 'FULLSIZE', 'HYBRID', 'AUTOMATIC', 'AWD', 2000,
        70.00),

       (8, 'Audi', 'A4', '2022', 'SEDAN', 4, 5, 460.0, 'LUXURY', 'PETROL', 'AUTOMATIC', 'AWD', 2500, 95.00),

       (9, 'Mercedes-Benz', 'C-Class', '2023', 'SEDAN', 4, 5, 475.0, 'PREMIUM', 'PETROL', 'AUTOMATIC', 'RWD',
        2200, 110.00),

       (10, 'Volkswagen', 'Golf', '2023', 'COMPACT', 4, 5, 380.0, 'MIDSIZE', 'PETROL', 'MANUAL', 'FWD', 1400,
        35.00);
SELECT setval('car_models_seq', (SELECT MAX(id) FROM car_models));

-- Car models - features
INSERT INTO car_models_features (car_model_id, feature_id)
VALUES (1, 1),
       (1, 4),
       (1, 5),
       (2, 1),
       (2, 2),
       (2, 4),
       (3, 1),
       (3, 2),
       (3, 3),
       (4, 4),
       (4, 6),
       (4, 7),
       (5, 1),
       (5, 2),
       (5, 8),
       (6, 1),
       (6, 5),
       (6, 9),
       (7, 1),
       (7, 4),
       (7, 6),
       (8, 1),
       (8, 2),
       (8, 3),
       (9, 1),
       (9, 2),
       (9, 7),
       (10, 1),
       (10, 4),
       (10, 10);

-- Vehicles (15)
INSERT INTO vehicles (id, license_plate, vin, car_model_id, status, km_travelled, pending_cleaning, pending_repair)
VALUES
-- Vehicles for Car Model ID 1
(1, 'ABC1234', '1HGCM82633A000001', 1, 'AVAILABLE', 25000.0, FALSE, FALSE),
(2, 'XYZ5678', '2T1BURHE9JC000002', 1, 'AVAILABLE', 60000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 2
(3, 'JKL9012', '3VWFE21C04M000003', 2, 'AVAILABLE', 120000.0, TRUE, TRUE),

-- Vehicles for Car Model ID 3
(4, 'DEF3456', 'WVWZZZ3CZ8E000004', 3, 'AVAILABLE', 30000.0, FALSE, FALSE),
(5, 'GHI7890', '1N4AL3AP2DN000005', 3, 'IN_MAINTENANCE', 91000.0, TRUE, TRUE),

-- Vehicles for Car Model ID 4
(6, 'LMN2345', '5NPDH4AE7DH000006', 4, 'AVAILABLE', 47000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 5
(7, 'PQR6789', '1FAFP34N96W000007', 5, 'AVAILABLE', 54000.0, FALSE, FALSE),
(8, 'STU9876', 'JHMFA16586S000008', 5, 'AVAILABLE', 23000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 6
(9, 'VWX4321', '2HGES26751H000009', 6, 'IN_MAINTENANCE', 89000.0, TRUE, TRUE),

-- Vehicles for Car Model ID 7
(10, 'YZA8765', '1J4FA39S34P000010', 7, 'AVAILABLE', 12000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 8
(11, 'BCD5432', '3D7MX48A77G000011', 8, 'AVAILABLE', 103000.0, TRUE, TRUE),
(12, 'EFG2198', 'KMHCT4AE0FU000012', 8, 'AVAILABLE', 48000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 9
(13, 'HIJ3276', '5XYKT3A1XDG000013', 9, 'AVAILABLE', 37000.0, FALSE, FALSE),

-- Vehicles for Car Model ID 10
(14, 'KLM7645', '2T3ZF4DV4BW000014', 10, 'AVAILABLE', 51000.0, FALSE, FALSE),
(15, 'NOP4321', '1G1PC5SB1E000015', 10, 'AVAILABLE', 81000.0, FALSE, FALSE);
SELECT setval('vehicles_seq', (SELECT MAX(id) FROM vehicles));

-- Notes
INSERT INTO notes (id, content, author, date, vehicle_id)
VALUES (1, 'Routine check completed. No issues found.', 'John Doe', '2024-03-01 10:15:30'::timestamp, 1),
       (2, 'Customer reported unusual noise from the engine.', 'Jane Smith', '2024-03-02 14:30:45'::timestamp, 5),
       (3, 'Oil and filter changed during scheduled maintenance.', 'Mike Johnson', '2024-03-02 14:30:45'::timestamp,
        12),
       (4, 'Minor scratches on the front bumper noted.', 'Emily Davis', '2024-03-04 11:10:00'::timestamp, 8),
       (5, 'Vehicle returned with a low fuel level. Reminder to refuel before next rental.', 'David Brown',
        '2024-03-05 16:45:22'::timestamp, 15),
       (6, 'Tires replaced after reaching wear limit.', 'Chris Wilson', '2024-03-06 08:05:55'::timestamp, 11),
       (7, 'Battery issue reported, testing required.', 'Sophia Martinez', '2024-03-07 12:30:40'::timestamp, 7),
       (8, 'Windshield wipers replaced.', 'James Anderson', '2024-03-08 14:55:10'::timestamp, 4),
       (9, 'Brake pads worn out, scheduled for replacement.', 'Olivia Taylor', '2024-03-09 17:20:30'::timestamp, 1),
       (10, 'Customer feedback: Excellent ride quality.', 'Daniel Thomas', '2024-03-10 13:10:00'::timestamp, 5),
       (11, 'Interior cleaning required after last rental.', 'Emma Harris', '2024-03-11 18:40:25'::timestamp, 11),
       (12, 'Check engine light turned on, diagnosing issue.', 'Michael Robinson', '2024-03-12 07:50:15'::timestamp,
        4),
       (13, 'Vehicle returned with minor dent on rear door.', 'Charlotte White', '2024-03-13 10:25:30'::timestamp, 3),
       (14, 'Navigation system needs an update.', 'Benjamin Lewis', '2024-03-14 16:10:55'::timestamp, 8),
       (15, 'New air freshener added after deep cleaning.', 'Mia Walker', '2024-03-15 12:00:00'::timestamp,
        15);
SELECT setval('notes_seq', (SELECT MAX(id) FROM notes));

-- Maintenance records
INSERT INTO maintenances (id, defects, type, upcoming_service_needs, start_date, planned_end_date,
                          actual_end_date, start_fleet_manager_username, end_fleet_manager_username, vehicle_id)
VALUES (1, 'Brake pads worn out', 'BRAKES', 'Check brake fluid in 5000 km',
        '2025-04-20 10:15:30'::timestamp, '2025-04-22 10:15:30'::timestamp, '2025-04-22 12:15:30'::timestamp,
        'fleetmanager', 'fleetmanager', 3),
       (2, 'Battery voltage low', 'BATTERY', 'May require replacement soon',
        '2024-03-02 14:30:45'::timestamp, '2024-03-05 16:30:45'::timestamp, '2024-03-05 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager', 7),
       (3, 'Engine oil level low', 'OIL_CHANGE', 'Next oil change due in 10,000 km',
        '2025-04-22 14:30:45'::timestamp, '2025-04-23 16:30:45'::timestamp, '2025-04-23 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager', 6),
       (4, 'Transmission fluid dirty', 'ENGINE', 'Flush and refill in next service',
        '2024-03-04 11:10:00'::timestamp, '2024-03-08 16:30:45'::timestamp, '2024-03-09 18:30:45'::timestamp,
        'fleetmanager', 'fleetmanager', 4),
       (5, 'Minor dent on rear door', 'CHASSIS', 'Inspect paint job next visit',
        '2024-03-05 16:45:22'::timestamp, '2024-03-06 18:45:22'::timestamp, '2024-03-06 20:45:22'::timestamp,
        'fleetmanager', 'fleetmanager', 8),
       (6, 'Check engine light on', 'OTHER', 'Further diagnostics required',
        '2024-03-06 08:05:55'::timestamp, '2024-03-06 10:05:55'::timestamp, '2024-03-06 12:05:55'::timestamp,
        'fleetmanager', 'fleetmanager', 6),
       (7, 'Tires tread low', 'TIRES', 'Next inspection in 5000 km', '2024-03-07 12:30:40'::timestamp,
        '2024-03-08 14:30:40'::timestamp, '2024-03-08 16:30:40'::timestamp, 'fleetmanager', 'fleetmanager', 11);
SELECT setval('maintenances_seq', (SELECT MAX(id) FROM maintenances));
