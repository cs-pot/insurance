package com.cspot.insurahub.config.auth0;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.exception.Auth0Exception;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
@RequiredArgsConstructor
public class Auth0Config {

    private final Auth0ConfigurationProperties configurationProperties;

    @Bean
    public ManagementApi managementApi() throws Auth0Exception {
        return ManagementApi.builder()
                .domain(configurationProperties.getDomain())
                .clientCredentials(configurationProperties.getClientId(), configurationProperties.getClientSecret())
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(Auth0PermissionsConverter permissionsConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(permissionsConverter);
        return converter;
    }
}