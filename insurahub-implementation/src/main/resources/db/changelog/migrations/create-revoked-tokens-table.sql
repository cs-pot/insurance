--liquibase formatted sql

--changeset OlaoluwaOlive:create-revoked-tokens-table
CREATE TABLE revoked_tokens (
    jti VARCHAR(255) PRIMARY KEY,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
