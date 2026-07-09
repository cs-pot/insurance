package com.cspot.insurahub.config.auth0;

import com.auth0.client.mgmt.ManagementApi;
import com.auth0.exception.Auth0Exception;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class Auth0Config {

    @Value("${auth0.domain}")
    private String domain;

    @Value("${auth0.client-id}")
    private String clientId;

    @Value("${auth0.client-secret}")
    private String clientSecret;

    @Value("${auth0.token}")
    private String token;

    @Bean
    public ManagementApi managementApi() throws Auth0Exception {
        return ManagementApi.builder()
                .domain(domain)
                .clientCredentials(clientId, clientSecret)
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter(Auth0PermissionsConverter permissionsConverter) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(permissionsConverter);
        return converter;
    }
}