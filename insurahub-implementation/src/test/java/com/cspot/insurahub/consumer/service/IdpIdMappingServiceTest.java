package com.cspot.insurahub.consumer.service;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import com.cspot.insurahub.consumer.exception.ConsumerNotFoundException;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdpIdMappingServiceTest {

    @Mock
    private ConsumerRepository consumerRepository;

    @Mock
    private AuthenticationMetadataQueryService authenticationMetadataQueryService;

    @InjectMocks
    private IdpIdMappingService service;

    @Test
    void shouldReturnConsumerIdWhenAuthenticatedPrincipalExistsAndConsumerFound() {
        // GIVEN
        String idpId = "idp-user-123";
        UUID consumerId = UUID.randomUUID();

        when(authenticationMetadataQueryService.getAuthenticatedPrincipalName())
                .thenReturn(Optional.of(idpId));
        when(consumerRepository.findIdByIdpId(idpId))
                .thenReturn(Optional.of(consumerId));

        // WHEN
        UUID result = service.getCurrentAuthenticatedConsumerId();

        // THEN
        assertThat(result).isEqualTo(consumerId);

        verify(authenticationMetadataQueryService).getAuthenticatedPrincipalName();
        verify(consumerRepository).findIdByIdpId(idpId);
    }

    @Test
    void shouldThrowWhenAuthenticatedPrincipalIsMissing() {
        // GIVEN
        when(authenticationMetadataQueryService.getAuthenticatedPrincipalName())
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(service::getCurrentAuthenticatedConsumerId)
                .isInstanceOf(InsufficientAuthenticationException.class);
    }

    @Test
    void shouldThrowWhenConsumerDoesNotExistForAuthenticatedPrincipal() {
        // GIVEN
        String idpId = "idp-user-123";

        when(authenticationMetadataQueryService.getAuthenticatedPrincipalName())
                .thenReturn(Optional.of(idpId));
        when(consumerRepository.findIdByIdpId(idpId))
                .thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(service::getCurrentAuthenticatedConsumerId)
                .isInstanceOf(ConsumerNotFoundException.class);

        verify(authenticationMetadataQueryService).getAuthenticatedPrincipalName();
        verify(consumerRepository).findIdByIdpId(idpId);
    }
}
