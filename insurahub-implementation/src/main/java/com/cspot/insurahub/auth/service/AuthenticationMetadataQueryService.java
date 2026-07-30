package com.cspot.insurahub.auth.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationMetadataQueryService {

    public Optional<String> getAuthenticatedPrincipalName() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || authentication.getName() == null
                || authentication.getName().isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(authentication.getName());
    }

    public String getAuthenticatedPrincipalNameForDeleteOperation() {
        return getAuthenticatedPrincipalName()
                .orElseThrow(() -> new InsufficientAuthenticationException(
                        "Principal name is required to perform a delete operation"
                ));
    }
}
