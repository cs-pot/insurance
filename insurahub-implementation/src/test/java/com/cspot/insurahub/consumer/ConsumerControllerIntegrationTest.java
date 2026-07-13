package com.cspot.insurahub.consumer;

import com.cspot.insurahub.BaseIntegrationTest;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@ActiveProfiles("test")
@Import(ConsumerControllerIntegrationTest.TestConfig.class)
class ConsumerControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConsumerRepository consumerRepository;

    @Autowired
    private IdentityProviderClient identityProviderClient;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @BeforeEach
    void setUp() {
        Mockito.reset(identityProviderClient);
    }

    @Test
    void shouldCreateConsumer() throws Exception {
        PostConsumerRequest createRequest = getConsumerCreateRequest();
        Mockito.when(identityProviderClient.registerUser(createRequest.getEmail(), createRequest.getPassword()))
                .thenReturn("auth0|consumer-123");

        String responseJson = mockMvc.perform(post("/consumers")
                        .with(jwt()
                                .jwt(jwt -> jwt.claim(
                                        "permissions",
                                        List.of("create:consumers")
                                ))
                                .authorities(jwt -> Objects.requireNonNull(
                                        jwtAuthenticationConverter.convert(jwt)
                                ).getAuthorities()))
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID consumerId = jsonMapper.readValue(responseJson, PostResponse.class).getId();
        Consumer savedConsumer = consumerRepository.findById(consumerId)
                .orElseThrow(() -> new AssertionError("Consumer must be persisted"));

        assertThat(savedConsumer.getId()).isEqualTo(consumerId);
        assertThat(savedConsumer.getIdpId()).isEqualTo("auth0|consumer-123");
        assertThat(savedConsumer.getEmail()).isEqualTo(createRequest.getEmail());
        assertThat(savedConsumer.getFirstName()).isEqualTo(createRequest.getFirstName());
        assertThat(savedConsumer.getLastName()).isEqualTo(createRequest.getLastName());
        assertThat(savedConsumer.getPersonalId()).isEqualTo(createRequest.getPersonalId());
        assertThat(savedConsumer.getDateOfBirth()).isEqualTo(createRequest.getDateOfBirth());
        assertThat(savedConsumer.getAddress()).isEqualTo(createRequest.getAddress());
        assertThat(savedConsumer.getCity()).isEqualTo(createRequest.getCity());

        verify(identityProviderClient).registerUser(createRequest.getEmail(), createRequest.getPassword());
        verify(identityProviderClient).addUserRole("auth0|consumer-123", IdpRole.CONSUMER);
        verifyNoMoreInteractions(identityProviderClient);
    }

    @Test
    void shouldRejectConsumerCreationWithoutAuthority() throws Exception {
        PostConsumerRequest createRequest = getConsumerCreateRequest();

        mockMvc.perform(post("/consumers")
                        .with(jwt())
                        .contentType("application/json")
                        .content(jsonMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        assertThat(consumerRepository.count()).isZero();
        verifyNoInteractions(identityProviderClient);
    }

    private PostConsumerRequest getConsumerCreateRequest() {
        return new PostConsumerRequest()
                .email("email@email.org")
                .password("SecurePassword123")
                .firstName("First Name")
                .lastName("Last Name")
                .personalId("12345678910")
                .dateOfBirth(LocalDate.of(2026, 7, 7))
                .address("Address")
                .city("City");
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        IdentityProviderClient identityProviderClient() {
            return Mockito.mock(IdentityProviderClient.class);
        }
    }
}
