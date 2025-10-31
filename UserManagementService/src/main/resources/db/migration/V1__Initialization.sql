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
VALUES (1, 'customer1', 'Mario', 'Rossi', 'customer1@gmail.com', '+393244896743', 'Via Verdi 22', 100, 'CUSTOMER'),

       (2, 'manager', 'Bob', 'Smith', 'manager@gmail.com', '+393337862123', 'Via Blu 34', 10, 'MANAGER'),

       (3, 'fleetmanager', 'Charlie', 'Brown', 'fleetmanager@gmail.com', '+393457865473', 'Via Rosa 23', 40, 'FLEET_MANAGER'),

       (4, 'staff', 'Diana', 'Prince', 'staff@gmail.com', '+393628901870', 'Via Marrone 86', 80, 'STAFF'),

       (5, 'customer2', 'Paolo', 'Bianchi', 'customer2@gmail.com', '+39333879645293', 'Via Rossi 34', 100, 'CUSTOMER'),

        (6, 'customer3', 'Francesco', 'Verdi', 'customer3@gmail.com', '+39867546992', 'Via Bianchi 57', 100, 'CUSTOMER');

SELECT setval('users_seq', (SELECT MAX(id) FROM users));