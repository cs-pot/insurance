--liquibase formatted sql

--changeset OlaoluwaOlive:create-enrollments-table
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY,
    consumer_id UUID NOT NULL,
    package_id UUID NOT NULL,
    election_amount NUMERIC(10, 2) NOT NULL,
    contribution_amount NUMERIC(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR,
    CONSTRAINT fk_enrollments_consumer FOREIGN KEY (consumer_id) REFERENCES consumers(id),
    CONSTRAINT fk_enrollments_package FOREIGN KEY (package_id) REFERENCES packages(id),
    CONSTRAINT chk_enrollments_election_non_negative CHECK (election_amount >= 0),
    CONSTRAINT chk_enrollments_contribution_non_negative CHECK (contribution_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_consumer_status
ON enrollments(consumer_id, status);
