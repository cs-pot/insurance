--liquibase formatted sql

--changeset cspot:create-denial-reasons-tables

CREATE TABLE IF NOT EXISTS denial_reasons
(
    id          UUID          NOT NULL,
    label       VARCHAR(255)  NOT NULL,
    description TEXT          NOT NULL,

    CONSTRAINT pk_denial_reasons
        PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS claim_denial_reasons
(
    claim_id         UUID NOT NULL,
    denial_reason_id UUID NOT NULL,

    CONSTRAINT pk_claim_denial_reasons
        PRIMARY KEY (claim_id, denial_reason_id),

    CONSTRAINT uq_claim_denial_reasons_claim
        UNIQUE (claim_id),

    CONSTRAINT fk_claim_denial_reasons_claim
        FOREIGN KEY (claim_id)
            REFERENCES claims (id),

    CONSTRAINT fk_claim_denial_reasons_reason
        FOREIGN KEY (denial_reason_id)
            REFERENCES denial_reasons (id)
);

CREATE INDEX idx_claim_denial_reasons_reason_id
    ON claim_denial_reasons (denial_reason_id);

--rollback DROP TABLE claim_denial_reasons;
--rollback DROP TABLE denial_reasons;

--changeset cspot:seed-denial-reasons

INSERT INTO denial_reasons
    (id, label, description)
VALUES
    (
        '00000000-0000-0000-0000-000000000001',
        'Invalid or missing receipt',
        'The receipt is missing, unreadable, or does not contain the information required to process the claim.'
    ),
    (
        '00000000-0000-0000-0000-000000000002',
        'Amount mismatch',
        'The amount submitted in the claim does not match the amount shown on the supporting documentation.'
    ),
    (
        '00000000-0000-0000-0000-000000000003',
        'Service date mismatch',
        'The service date in the claim does not match the date shown on the supporting documentation.'
    ),
    (
        '00000000-0000-0000-0000-000000000004',
        'Outside coverage period',
        'The service was provided outside the period covered by the insurance plan.'
    ),
    (
        '00000000-0000-0000-0000-000000000005',
        'Exceeds coverage limit',
        'The requested reimbursement exceeds the coverage limit available under the insurance plan.'
    ),
    (
        '00000000-0000-0000-0000-000000000006',
        'Duplicate claim',
        'This claim appears to have already been submitted or reimbursed.'
    ),
    (
        '00000000-0000-0000-0000-000000000007',
        'Other',
        'The claim cannot be approved for another reason not covered by the predefined options.'
    )
ON CONFLICT (id) DO NOTHING;

--rollback DELETE FROM denial_reasons;
