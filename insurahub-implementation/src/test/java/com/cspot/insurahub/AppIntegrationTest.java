package com.cspot.insurahub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ActiveProfiles("test")
class AppIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void helloWorld() throws Exception {
        mockMvc.perform(get("/api/public/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, World!"));
    }

    @Test
    void privateHelloWorld() throws Exception {
        mockMvc.perform(get("/api/private/hello"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void privateHelloWorldWithJwt() throws Exception {
        mockMvc.perform(get("/api/private/hello").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("Private Hello, World!"));
    }

    @Test
    void privateEndpointReturnsPrincipalWithoutClaims() throws Exception {
        mockMvc.perform(get("/api/private").with(jwt()
                        .jwt(jwt -> jwt.subject("auth0|user-123"))
                        .authorities(new SimpleGrantedAuthority("SCOPE_read:private"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user").value("auth0|user-123"))
                .andExpect(jsonPath("$.scopes[0]").value("SCOPE_read:private"))
                .andExpect(jsonPath("$.claims").doesNotExist());
    }
}


    