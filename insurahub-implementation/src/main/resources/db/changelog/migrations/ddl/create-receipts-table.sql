--liquibase formatted sql

--changeset cspot:create-receipts-table

CREATE TABLE IF NOT EXISTS receipts
(
    id            UUID         NOT NULL,
    claim_id      UUID         NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100) NOT NULL,
    storage_type  VARCHAR(32)  NOT NULL,
    storage_key   VARCHAR(512) NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    content       BYTEA,

    created_at    TIMESTAMPTZ  NOT NULL,
    created_by    VARCHAR(255) NOT NULL,

    CONSTRAINT pk_receipts
        PRIMARY KEY (id),

    CONSTRAINT uk_receipts_storage_key
        UNIQUE (storage_key),

    CONSTRAINT fk_receipts_claim
        FOREIGN KEY (claim_id)
            REFERENCES claims (id),

    CONSTRAINT chk_receipts_storage_type
        CHECK (storage_type IN ('POSTGRES', 'S3')),

    CONSTRAINT chk_receipts_content_type
        CHECK (
            content_type IN (
                'application/pdf',
                'image/jpeg',
                'image/png'
            )
        ),

    CONSTRAINT chk_receipts_size
        CHECK (
            size_bytes > 0
            AND size_bytes < 10485760
        ),

    CONSTRAINT chk_receipts_content
        CHECK (
            (storage_type = 'POSTGRES' AND content IS NOT NULL)
            OR
            (storage_type = 'S3' AND content IS NULL)
        )
);

CREATE INDEX idx_receipts_claim_id
    ON receipts (claim_id);

--rollback DROP TABLE receipts;
