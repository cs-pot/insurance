package com.cspot.insurahub.config;

import com.cspot.insurahub.service.TokenBlacklistService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class BlacklistJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtAuthenticationConverter defaultConverter = new JwtAuthenticationConverter();
    private final TokenBlacklistService tokenBlacklistService;

    public BlacklistJwtAuthenticationConverter(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        // Fail early if required claims are missing
        if (jti == null || expiresAt == null) {
            OAuth2Error error = new OAuth2Error("invalid_token", "Token is missing required claims (jti or exp)", null);
            throw new OAuth2AuthenticationException(error);
        }

        if (tokenBlacklistService.isBlacklisted(jti)) {
            OAuth2Error error = new OAuth2Error("invalid_token", "Token has been revoked", null);
            throw new OAuth2AuthenticationException(error);
        }

        return defaultConverter.convert(jwt);
    }
}
