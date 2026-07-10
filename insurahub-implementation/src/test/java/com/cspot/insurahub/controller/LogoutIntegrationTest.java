package com.cspot.insurahub.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import com.cspot.insurahub.repository.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class LogoutIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void logoutShouldReturnNoContentAndBlacklistToken() throws Exception {
        revokedTokenRepository.deleteAll();

        mockMvc.perform(post("/auth/logout")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("jti", "test-jti-123")
                                .expiresAt(Instant.now().plusSeconds(300)))))
                .andExpect(status().isNoContent());

        assertThat(revokedTokenRepository.existsById("test-jti-123")).isTrue();
    }

    @Test
    void logoutWithoutTokenShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isUnauthorized());
    }
}
