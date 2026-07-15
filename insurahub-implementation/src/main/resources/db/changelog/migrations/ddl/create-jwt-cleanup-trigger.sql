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

CREATE TRIGGER IF NOT EXISTS blacklisted_jwt_cleanup_trigger AFTER INSERT
ON revoked_tokens
EXECUTE FUNCTION cleanup_blacklisted_tokens();
