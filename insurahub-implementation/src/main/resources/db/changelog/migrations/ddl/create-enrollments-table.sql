--liquibase formatted sql

--changeset OlaoluwaOlive:create-enrollments-table
CREATE TABLE IF NOT EXISTS enrollments (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    consumer_id UUID NOT NULL,
    plan_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR,
    CONSTRAINT fk_enrollments_consumer FOREIGN KEY (consumer_id) REFERENCES consumers(id),
    CONSTRAINT fk_enrollments_plan FOREIGN KEY (plan_id) REFERENCES plans(id)
);

CREATE INDEX IF NOT EXISTS idx_enrollments_consumer_status
ON enrollments(consumer_id, status);
