--liquibase formatted sql

--changeset cspot:create-claims-table

CREATE TABLE IF NOT EXISTS claims
(
    id               UUID           NOT NULL,
    version          BIGINT         NOT NULL DEFAULT 0,
    employee_id      UUID           NOT NULL,
    plan_id          UUID           NOT NULL,
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

    CONSTRAINT fk_claims_employee
        FOREIGN KEY (employee_id)
            REFERENCES consumers (id),

    CONSTRAINT fk_claims_plan
        FOREIGN KEY (plan_id)
            REFERENCES plans (id),

    CONSTRAINT chk_claims_amount_positive
        CHECK (amount > 0),

    CONSTRAINT chk_claims_status
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
);

CREATE INDEX idx_claims_employee_id
    ON claims (employee_id);

CREATE INDEX idx_claims_plan_id
    ON claims (plan_id);

CREATE INDEX idx_claims_employee_created_at
    ON claims (employee_id, created_at DESC);

--rollback DROP TABLE claims;
