package com.cspot.insurahub.consumer.service;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import com.cspot.insurahub.consumer.exception.ConsumerNotFoundException;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class IdpIdMappingService {

    private final ConsumerRepository consumerRepository;
    private final AuthenticationMetadataQueryService authenticationMetadataQueryService;

    public UUID getCurrentAuthenticatedConsumerId() {
        String idpId = getAuthenticatedPrincipalName();
        return consumerRepository.findIdByIdpId(idpId)
                .orElseThrow(() -> {
                    String message = "Failed to find consumer with idpId " + idpId;
                    return new ConsumerNotFoundException(message);
                });
    }

    private String getAuthenticatedPrincipalName() {
        return authenticationMetadataQueryService.getAuthenticatedPrincipalName()
                .orElseThrow(() -> new InsufficientAuthenticationException("Failed to get idpId"));
    }
}
