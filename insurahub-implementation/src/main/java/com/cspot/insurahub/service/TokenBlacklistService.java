package com.cspot.insurahub.service;

import com.cspot.insurahub.entity.RevokedToken;
import com.cspot.insurahub.repository.RevokedTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public void blacklistToken(String jti, Instant expiresAt) {
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
