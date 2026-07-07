--liquibase formatted sql

--changeset cspot:001-create-test-table
CREATE TABLE test_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);