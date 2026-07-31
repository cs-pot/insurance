package com.cspot.insurahub.auth.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticationMetadataQueryServiceTest {

    private final AuthenticationMetadataQueryService service =
            new AuthenticationMetadataQueryService();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnEmptyWhenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        Optional<String> result = service.getAuthenticatedPrincipalName();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenAuthenticationIsNotAuthenticated() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("user", "password");
        authentication.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> result = service.getAuthenticatedPrincipalName();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyWhenAuthenticationIsAnonymous() {
        AnonymousAuthenticationToken authentication =
                new AnonymousAuthenticationToken(
                        "key",
                        "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> result = service.getAuthenticatedPrincipalName();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnPrincipalNameWhenAuthenticated() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("john.doe", "password");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> result = service.getAuthenticatedPrincipalName();

        assertEquals(Optional.of("john.doe"), result);
    }

    @Test
    void shouldReturnEmptyWhenPrincipalNameIsNull() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken(null, "password");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Optional<String> result = service.getAuthenticatedPrincipalName();

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldThrowWhenPrincipalNameIsRequiredForDeleteOperation() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(service::getRequiredAuthenticatedPrincipalName)
                .isInstanceOf(InsufficientAuthenticationException.class)
                .hasMessage("Principal name is required to perform a delete operation");
    }

    @Test
    void shouldReturnPrincipalNameForDeleteOperation() {
        TestingAuthenticationToken authentication =
                new TestingAuthenticationToken("john.doe", "password");
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertEquals("john.doe", service.getRequiredAuthenticatedPrincipalName());
    }
}
