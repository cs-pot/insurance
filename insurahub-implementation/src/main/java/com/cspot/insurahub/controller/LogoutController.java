package com.cspot.insurahub.controller;

import com.cspot.insurahub.service.TokenBlacklistService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class LogoutController {

    private final TokenBlacklistService tokenBlacklistService;

    public LogoutController(TokenBlacklistService tokenBlacklistService) {
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getId() == null || jwt.getExpiresAt() == null) {
            // Return 400 if required token claims are missing
            return ResponseEntity.badRequest().build();
        }

        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();
        
        tokenBlacklistService.blacklistToken(jti, expiresAt);
        
        return ResponseEntity.noContent().build();
    }
}
