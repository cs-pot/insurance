--liquibase formatted sql

--changeset cspot:create-claims-table

CREATE TABLE IF NOT EXISTS claims
(
    id               UUID           NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    enrollment_id    UUID           NOT NULL,
    service_date     DATE           NOT NULL,
    amount           NUMERIC(12, 2) NOT NULL,
    status           VARCHAR(32)    NOT NULL DEFAULT 'PENDING',

    created_at       TIMESTAMPTZ    NOT NULL,
    created_by       VARCHAR(255)   NOT NULL,
    updated_at       TIMESTAMPTZ,
    updated_by       VARCHAR(255),
    deleted_at       TIMESTAMPTZ,
    deleted_by       VARCHAR(255),

    CONSTRAINT pk_claims
        PRIMARY KEY (id),

    CONSTRAINT fk_claims_enrollment
        FOREIGN KEY (enrollment_id)
            REFERENCES enrollments (id),

    CONSTRAINT chk_claims_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_claims_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_claims_enrollment_id
    ON claims (enrollment_id);

CREATE INDEX idx_claims_enrollment_created_at
    ON claims (enrollment_id, created_at DESC);

--rollback DROP TABLE claims;
