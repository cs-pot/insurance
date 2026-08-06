--liquibase formatted sql

--changeset cspot:add-claim-number-column-and-sequences

ALTER TABLE claims
    ADD COLUMN claim_number VARCHAR(13);

WITH numbered_claims AS (
    SELECT
        id,
        'LT' || TO_CHAR(service_date, 'YYYYMMDD')
            || LPAD(ROW_NUMBER() OVER (
                PARTITION BY service_date
                ORDER BY created_at, id
            )::TEXT, 3, '0') AS generated_claim_number
    FROM claims
)
UPDATE claims
SET claim_number = numbered_claims.generated_claim_number
FROM numbered_claims
WHERE claims.id = numbered_claims.id;

ALTER TABLE claims
    ALTER COLUMN claim_number SET NOT NULL;

ALTER TABLE claims
    ADD CONSTRAINT uq_claims_claim_number
        UNIQUE (claim_number);

ALTER TABLE claims
    ADD CONSTRAINT chk_claims_claim_number_format
        CHECK (claim_number ~ '^LT[0-9]{11}$');

CREATE TABLE claim_number_sequences
(
    service_date DATE    NOT NULL,
    last_number  INTEGER NOT NULL,

    CONSTRAINT pk_claim_number_sequences
        PRIMARY KEY (service_date),

    CONSTRAINT chk_claim_number_sequences_last_number
        CHECK (last_number BETWEEN 1 AND 999)
);

INSERT INTO claim_number_sequences (service_date, last_number)
SELECT
    service_date,
    MAX(SUBSTRING(claim_number FROM 11 FOR 3)::INTEGER)
FROM claims
GROUP BY service_date;

--rollback DROP TABLE claim_number_sequences;
--rollback ALTER TABLE claims DROP CONSTRAINT chk_claims_claim_number_format;
--rollback ALTER TABLE claims DROP CONSTRAINT uq_claims_claim_number;
--rollback ALTER TABLE claims DROP COLUMN claim_number;

--changeset cspot:create-generate-claim-number-function splitStatements:false
CREATE OR REPLACE FUNCTION generate_claim_number()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    next_number INTEGER;
BEGIN
    IF NEW.claim_number IS NOT NULL THEN
        INSERT INTO claim_number_sequences (service_date, last_number)
        VALUES (NEW.service_date, SUBSTRING(NEW.claim_number FROM 11 FOR 3)::INTEGER)
        ON CONFLICT (service_date) DO UPDATE
            SET last_number = GREATEST(
                claim_number_sequences.last_number,
                EXCLUDED.last_number
            );

        RETURN NEW;
    END IF;

    INSERT INTO claim_number_sequences (service_date, last_number)
    VALUES (NEW.service_date, 1)
    ON CONFLICT (service_date) DO UPDATE
        SET last_number = claim_number_sequences.last_number + 1
    RETURNING last_number INTO next_number;

    IF next_number > 999 THEN
        RAISE EXCEPTION 'Daily claim number sequence exhausted for service date: %', NEW.service_date;
    END IF;

    NEW.claim_number = 'LT' || TO_CHAR(NEW.service_date, 'YYYYMMDD') || LPAD(next_number::TEXT, 3, '0');
    RETURN NEW;
END;
$$;

--rollback DROP FUNCTION generate_claim_number();

--changeset cspot:create-generate-claim-number-trigger
CREATE TRIGGER trg_generate_claim_number
    BEFORE INSERT ON claims
    FOR EACH ROW
    EXECUTE FUNCTION generate_claim_number();

--rollback DROP TRIGGER trg_generate_claim_number ON claims;
