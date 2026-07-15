package com.cspot.insurahub.consumer.controller;

import com.cspot.insurahub.BaseIntegrationTest;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.enumeration.IdpRole;
import com.cspot.insurahub.consumer.identity.IdentityProviderClient;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import com.cspot.insurahub.model.PostConsumerRequest;
import com.cspot.insurahub.model.PostResponse;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
                        .with(jwtWithPermission("create:consumers"))
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

    @Test
    @Sql("/consumer/seed-consumers.sql")
    void shouldReturnConsumers() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("page", "0")
                        .param("size", "2")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.content[0].firstName").value("First"))
                .andExpect(jsonPath("$.content[0].lastName").value("Consumer"))
                .andExpect(jsonPath("$.content[0].fullName").value("First Consumer"))
                .andExpect(jsonPath("$.content[0].personalId").value("12345678910"))
                .andExpect(jsonPath("$.content[0].dateOfBirth").value("2026-07-07"))
                .andExpect(jsonPath("$.content[1].id").value("22222222-2222-2222-2222-222222222222"))
                .andExpect(jsonPath("$.content[1].firstName").value("Second"))
                .andExpect(jsonPath("$.content[1].lastName").value("Consumer"))
                .andExpect(jsonPath("$.content[1].fullName").value("Second Consumer"))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2))
                .andExpect(jsonPath("$.pageable").doesNotExist())
                .andExpect(jsonPath("$.sort").doesNotExist());
    }

    @Test
    @Sql("/consumer/seed-consumers.sql")
    void shouldReturnRequestedConsumerPage() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("page", "1")
                        .param("size", "2")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value("33333333-3333-3333-3333-333333333333"))
                .andExpect(jsonPath("$.content[0].firstName").value("Third"))
                .andExpect(jsonPath("$.content[0].lastName").value("Consumer"))
                .andExpect(jsonPath("$.content[0].fullName").value("Third Consumer"))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    void shouldReturnEmptyConsumerList() throws Exception {
        mockMvc.perform(get("/consumers")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    void shouldRejectNegativeConsumerPage() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("page", "-1")
                        .param("size", "20")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("page must be greater than or equal to 0"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/consumers"));
    }

    @Test
    void shouldRejectNegativeConsumerPageSize() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("page", "0")
                        .param("size", "-20")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("size must be greater than or equal to 1"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/consumers"));
    }

    @Test
    void shouldRejectUnsupportedConsumerSortProperty() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("sort", "personalId,asc")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message")
                        .value("sort property must be one of: createdAt, firstName, lastName, email"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/consumers"));
    }

    @Test
    void shouldRejectUnsupportedConsumerSortDirection() throws Exception {
        mockMvc.perform(get("/consumers")
                        .param("sort", "firstName,ascending")
                        .with(jwtWithPermission("view:consumers")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("sort direction must be asc or desc"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/consumers"));
    }

    @Test
    void shouldRejectConsumerListWithoutAuthority() throws Exception {
        mockMvc.perform(get("/consumers")
                        .with(jwt()))
                .andExpect(status().isForbidden());

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

    private RequestPostProcessor jwtWithPermission(String permission) {
        return jwt()
                .jwt(jwt -> jwt.claim("permissions", List.of(permission)))
                .authorities(jwt -> Objects.requireNonNull(jwtAuthenticationConverter.convert(jwt)).getAuthorities());
    }

    private Consumer getConsumer() {
        Consumer consumer = new Consumer();
        consumer.setIdpId("auth0|consumer-123");
        consumer.setEmail("email@email.org");
        consumer.setFirstName("First Name");
        consumer.setLastName("Last Name");
        consumer.setPersonalId("12345678910");
        consumer.setDateOfBirth(LocalDate.of(2026, 7, 7));
        consumer.setAddress("Address");
        consumer.setCity("City");
        return consumer;
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
