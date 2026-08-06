--liquibase formatted sql

--changeset cspot:rename-claim-status-rejected-to-denied splitStatements:false
ALTER TABLE claims
    DROP CONSTRAINT IF EXISTS chk_claims_status;

UPDATE claims
    SET status = 'DENIED'
    WHERE status = 'REJECTED';

ALTER TABLE claims
    ADD CONSTRAINT chk_claims_status CHECK (status IN ('PENDING', 'APPROVED', 'DENIED'));

--rollback ALTER TABLE claims DROP CONSTRAINT IF EXISTS chk_claims_status;
--rollback UPDATE claims SET status = 'REJECTED' WHERE status = 'DENIED';
--rollback ALTER TABLE claims ADD CONSTRAINT chk_claims_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));
