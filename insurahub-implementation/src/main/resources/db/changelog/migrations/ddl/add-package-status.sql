--liquibase formatted sql

--changeset cspot:add-package-status splitStatements:false
ALTER TABLE packages
    ADD COLUMN IF NOT EXISTS status VARCHAR(50) NOT NULL DEFAULT 'NOT_STARTED';

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_packages_status'
    ) THEN
        ALTER TABLE packages
            ADD CONSTRAINT chk_packages_status CHECK (status IN ('NOT_STARTED', 'INITIALIZED'));
    END IF;
END
$$;
