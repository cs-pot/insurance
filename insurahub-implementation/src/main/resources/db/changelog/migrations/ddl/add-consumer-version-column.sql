--liquibase formatted sql

--changeset cspot:add-consumer-version-column
ALTER TABLE consumers ADD COLUMN version BIGSERIAL NOT NULL;