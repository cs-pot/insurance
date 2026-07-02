package com.cspot.insurahub;

import com.cspot.insurahub.entity.TestEntity;
import com.cspot.insurahub.repository.TestEntityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


@Testcontainers
@SpringBootTest(properties = {
    "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://test.auth0.com/",
    "spring.security.oauth2.resourceserver.jwt.audiences=https://insurahub.test/api"
})
@AutoConfigureMockMvc
class InsuraHubApplicationTests {

    @Autowired
    private TestEntityRepository repository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18");

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
    void shouldPersistEntity() {
        TestEntity entity = new TestEntity("John");

        repository.save(entity);

        assertEquals(1, repository.count());
    }
}
