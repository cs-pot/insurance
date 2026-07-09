package com.cspot.insurahub.service;

import com.cspot.insurahub.entity.RevokedToken;
import com.cspot.insurahub.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void blacklistTokenFromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new IllegalArgumentException("Invalid authentication token");
        }

        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();

        if (jti == null || expiresAt == null) {
            throw new IllegalArgumentException("Token is missing required claims (jti or exp)");
        }

        if (!isBlacklisted(jti)) {
            RevokedToken token = new RevokedToken(jti, expiresAt);
            revokedTokenRepository.save(token);
        }
    }

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String jti) {
        return revokedTokenRepository.existsById(jti);
    }
}
