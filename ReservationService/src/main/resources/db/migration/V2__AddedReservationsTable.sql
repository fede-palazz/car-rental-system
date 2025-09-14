CREATE SEQUENCE IF NOT EXISTS reservations_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE reservations
(
    id                       BIGINT                      NOT NULL,
    customer_username        VARCHAR(255)                NOT NULL,
    vehicle_id               BIGINT                      NOT NULL,
    creation_date            TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    planned_pick_up_date     TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_pick_up_date      TIMESTAMP WITHOUT TIME ZONE,
    planned_drop_off_date    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    actual_drop_off_date     TIMESTAMP WITHOUT TIME ZONE,
    status                   VARCHAR(255)                NOT NULL,
    total_amount             DOUBLE PRECISION            NOT NULL,
    was_delivery_late        BOOLEAN,
    was_charged_fee          BOOLEAN,
    was_involved_in_accident BOOLEAN,
    damage_level             INTEGER,
    dirtiness_level          INTEGER,
    pick_up_staff_username   VARCHAR(255),
    drop_off_staff_username  VARCHAR(255),
    CONSTRAINT pk_reservations PRIMARY KEY (id)
);

ALTER TABLE reservations
    ADD CONSTRAINT FK_RESERVATIONS_ON_VEHICLE FOREIGN KEY (vehicle_id) REFERENCES vehicles (id);

-- Reservation 1: Successful reservation with no issues
INSERT INTO reservations (
    id, customer_username, vehicle_id, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username
) VALUES (
             1, 'customer1', 1, '2025-04-01 09:00:00',
             '2025-04-03 10:00:00', '2025-04-03 10:00:00',
             '2025-04-10 10:00:00', '2025-04-10 09:45:00',
             'CONFIRMED',
             320.00,
             NULL, FALSE, FALSE, 0, 0, 'staff', 'staff'
         );


-- Reservation 2: Late delivery and damage, still completed
INSERT INTO reservations (
    id, customer_username, vehicle_id, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username
) VALUES (
             2, 'customer2', 2, '2025-04-05 12:00:00',
             '2025-04-06 09:00:00', '2025-04-06 10:30:00',
             '2025-04-12 09:00:00', '2025-04-12 10:00:00',
             'CONFIRMED',
             280.00,
             TRUE, TRUE, TRUE, 2, 1, 'staff', 'staff'
         );

-- Reservation 3: Cancelled before pickup
INSERT INTO reservations (
    id, customer_username, vehicle_id, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username
) VALUES (
             3, 'customer1', 3, '2025-04-07 15:00:00',
             '2025-04-09 08:00:00', NULL,
             '2025-05-15 08:00:00', NULL,
             'CONFIRMED',
             269.50,
             NULL, NULL, NULL, NULL, NULL, NULL, NULL
         );

-- Reservation 4: Accident involved
INSERT INTO reservations (
    id, customer_username, vehicle_id, creation_date,
    planned_pick_up_date, actual_pick_up_date,
    planned_drop_off_date, actual_drop_off_date,
    status, total_amount,
    was_delivery_late, was_charged_fee, was_involved_in_accident,
    damage_level, dirtiness_level, pick_up_staff_username, drop_off_staff_username
) VALUES (
             4, 'customer2', 4, '2025-04-10 11:00:00',
             '2025-04-11 09:00:00', '2025-04-11 09:15:00',
             '2025-04-17 09:00:00', '2025-04-17 10:00:00',
             'CONFIRMED',
             700.00,
             TRUE, TRUE, TRUE, 3, 2, 'staff', 'staff'
         );
SELECT setval('reservations_seq', (SELECT MAX(id) FROM reservations));