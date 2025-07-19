-- Initial schema

-- V1__Initialization.sql
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

ALTER TABLE user_preferences
    ADD CONSTRAINT FK_USER_PREFERENCES_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);