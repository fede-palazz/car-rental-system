CREATE SEQUENCE IF NOT EXISTS user_preferences_seq START WITH 1 INCREMENT BY 50;

CREATE SEQUENCE IF NOT EXISTS users_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE user_preferences
(
    id                 BIGINT NOT NULL,
    user_id            BIGINT,
    preferred_segments TEXT,
    favorite_brands    TEXT,
    favorite_features  TEXT,
    CONSTRAINT pk_user_preferences PRIMARY KEY (id)
);

CREATE TABLE users
(
    id                BIGINT       NOT NULL,
    username          VARCHAR(255) NOT NULL,
    first_name        VARCHAR(255) NOT NULL,
    last_name         VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL,
    role              VARCHAR(255) NOT NULL,
    phone             VARCHAR(255) NOT NULL,
    address           VARCHAR(255) NOT NULL,
    eligibility_score DOUBLE PRECISION,
    CONSTRAINT pk_users PRIMARY KEY (id)
);

ALTER TABLE users
    ADD CONSTRAINT uc_users_email UNIQUE (email);

ALTER TABLE users
    ADD CONSTRAINT uc_users_username UNIQUE (username);

ALTER TABLE user_preferences
    ADD CONSTRAINT FK_USER_PREFERENCES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);

-- Initial data

INSERT INTO users (id, username, first_name, last_name, email, phone, address, eligibility_score, role)
VALUES (1, 'customer1', 'Alice', 'Johnson', 'customer1@example.com', '123-456-7890', '123 Main St', 100, 'CUSTOMER'),

       (2, 'manager', 'Bob', 'Smith', 'manager@example.com', '321-654-0987', '456 Elm St', 10, 'MANAGER'),

       (3, 'fleetmanager', 'Charlie', 'Brown', 'fleet.manager@example.com', '456-234-6742', '678 Park Avenue', 40, 'FLEET_MANAGER'),

       (4, 'staff', 'Diana', 'Prince', 'staff@example.com', '555-123-4567', '789 Oak Ave', 80, 'STAFF'),

       (5, 'customer2', 'Jonathan', 'John', 'customer2@example.com', '123-456-7891', '123 Main St', 100, 'CUSTOMER');
SELECT setval('users_seq', (SELECT MAX(id) FROM users));