--liquibase formatted sql

--changeset cspot:reate-consumers-table
CREATE TABLE IF NOT EXISTS consumers (
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    id UUID PRIMARY KEY,
    idp_id VARCHAR UNIQUE NOT NULL,
    email VARCHAR NOT NULL,
    first_name VARCHAR NOT NULL,
    last_name VARCHAR NOT NULL,
    personal_id VARCHAR NOT NULL,
    date_of_birth DATE NOT NULL,
    address VARCHAR NOT NULL,
    city VARCHAR NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS consumers_email_unique
ON consumers(LOWER(email))
WHERE deleted = false;
