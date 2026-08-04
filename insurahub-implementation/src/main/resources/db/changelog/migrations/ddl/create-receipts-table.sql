--liquibase formatted sql

--changeset cspot:create-receipts-table

CREATE TABLE IF NOT EXISTS receipts
(
    id            UUID         NOT NULL,
    claim_id      UUID         NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    content       BYTEA        NOT NULL,

    created_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(255) NOT NULL,

    CONSTRAINT pk_receipts
        PRIMARY KEY (id),

    CONSTRAINT uk_receipts_claim_id
        UNIQUE (claim_id),

    CONSTRAINT fk_receipts_claim
        FOREIGN KEY (claim_id)
            REFERENCES claims (id)
);

--rollback DROP TABLE receipts;
