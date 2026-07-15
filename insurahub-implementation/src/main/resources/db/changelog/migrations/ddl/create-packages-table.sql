--liquibase formatted sql

--changeset cspot:create-packages-table
CREATE TABLE IF NOT EXISTS packages (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(255) NOT NULL,
    payroll VARCHAR(50) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR NOT NULL,
    updated_at TIMESTAMPTZ,
    updated_by VARCHAR,
    deleted_at TIMESTAMPTZ,
    deleted_by VARCHAR,
    CONSTRAINT chk_packages_name_not_blank CHECK (length(btrim(name)) > 0),
    CONSTRAINT chk_packages_payroll CHECK (payroll IN ('WEEKLY', 'BI_WEEKLY', 'MONTHLY', 'ANNUALLY')),
    CONSTRAINT chk_packages_dates CHECK (end_date >= start_date),
    CONSTRAINT chk_packages_payroll_period CHECK (
        end_date >= CASE payroll
            WHEN 'WEEKLY' THEN start_date + 6
            WHEN 'BI_WEEKLY' THEN start_date + 13
            WHEN 'MONTHLY' THEN (start_date + INTERVAL '1 month' - INTERVAL '1 day')::date
            WHEN 'ANNUALLY' THEN (start_date + INTERVAL '1 year' - INTERVAL '1 day')::date
        END
    )
);
