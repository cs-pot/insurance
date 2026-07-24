--liquibase formatted sql

--changeset cspot:create-plans-table
CREATE TABLE IF NOT EXISTS plans (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    package_id UUID NOT NULL,
    name VARCHAR(50) NOT NULL,
    type VARCHAR(50) NOT NULL,
    contribution NUMERIC(10, 2) NOT NULL,
    election NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR,
    CONSTRAINT fk_plans_package_id FOREIGN KEY (package_id) REFERENCES packages (id),
    CONSTRAINT chk_plans_name CHECK (name ~ '^[A-Za-z ]+$' AND name ~ '[A-Za-z]' AND length(name) <= 50),
    CONSTRAINT chk_plans_type CHECK (type IN ('HEALTH_INSURANCE', 'DENTAL_INSURANCE', 'VISION_INSURANCE')),
    CONSTRAINT chk_plans_contribution_range CHECK (contribution BETWEEN 10 AND 1000),
    CONSTRAINT chk_plans_election_range CHECK (election BETWEEN 10 AND 1000)
);
