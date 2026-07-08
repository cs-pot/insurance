package com.cspot.insurahub.controller;

import com.cspot.insurahub.repository.RevokedTokenRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LogoutIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Test
    void logoutShouldReturnNoContentAndBlacklistToken() throws Exception {
        // 1. Ensure the table is empty before the test
        revokedTokenRepository.deleteAll();

        // 2. Call the endpoint with a mocked JWT
        mockMvc.perform(post("/api/logout")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("jti", "test-jti-123") // Mocked JTI
                                .claim("exp", Instant.now().plusSeconds(300))))) // Mocked Expiry
                .andExpect(status().isNoContent()); // Expect 204

        // 3. Verify the token was actually saved to the database blacklist
        assertThat(revokedTokenRepository.existsById("test-jti-123")).isTrue();
    }

    @Test
    void logoutWithoutTokenShouldReturnUnauthorized() throws Exception {
        // Call the endpoint without a token -> Should be 401
        mockMvc.perform(post("/api/logout"))
                .andExpect(status().isUnauthorized());
    }
}
