package com.cspot.insurahub.auth.config;

import com.cspot.insurahub.auth.service.TokenBlacklistService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BlacklistTokenValidator implements OAuth2TokenValidator<Jwt> {

    private final TokenBlacklistService tokenBlacklistService;

    public BlacklistTokenValidator(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        if (jti == null || expiresAt == null) {
            OAuth2Error error = new OAuth2Error("invalid_token", "Token is missing required claims (jti or exp)", null);
            return OAuth2TokenValidatorResult.failure(error);
        }

        if (tokenBlacklistService.isBlacklisted(jti)) {
            OAuth2Error error = new OAuth2Error("invalid_token", "Token has been revoked", null);
            return OAuth2TokenValidatorResult.failure(error);
        }

        return OAuth2TokenValidatorResult.success();
    }
}
