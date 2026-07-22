--liquibase formatted sql

--changeset cspot:create-jwt-cleanup-trigger splitStatements:false
CREATE OR REPLACE FUNCTION cleanup_blacklisted_tokens()
RETURNS trigger
AS $$
BEGIN
    DELETE
    FROM revoked_tokens
    WHERE expires_at < now();
    RETURN NEW;
END;
$$
LANGUAGE plpgsql;

CREATE OR REPLACE TRIGGER blacklisted_jwt_cleanup_trigger AFTER INSERT
ON revoked_tokens
EXECUTE FUNCTION cleanup_blacklisted_tokens();

CREATE INDEX IF NOT EXISTS idx_revoked_tokens_expires_at
ON revoked_tokens(expires_at);
