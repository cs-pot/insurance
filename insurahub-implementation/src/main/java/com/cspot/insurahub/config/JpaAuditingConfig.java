package com.cspot.insurahub.config;

import com.cspot.insurahub.auth.service.AuthenticationMetadataQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

@Configuration
@RequiredArgsConstructor
@EnableJpaAuditing(
        auditorAwareRef = "auditorProvider",
        modifyOnCreate = false
)
public class JpaAuditingConfig {

    private final AuthenticationMetadataQueryService authenticationMetadataQueryService;

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            String principalName = authenticationMetadataQueryService
                    .getAuthenticatedPrincipalName().orElse("system");
            return Optional.of(principalName);
        };
    }
}
