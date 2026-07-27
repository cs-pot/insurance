--liquibase formatted sql

--changeset cspot:update-package-status-lifecycle-values splitStatements:false
ALTER TABLE packages
    DROP CONSTRAINT IF EXISTS chk_packages_status;

ALTER TABLE packages
    ADD CONSTRAINT chk_packages_status CHECK (status IN ('NOT_STARTED', 'INITIALIZED', 'ACTIVE', 'ARCHIVED'));
