package com.cspot.insurahub.config;

import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.OffsetDateTime;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final String PERMISSIONS_CLAIM = "permissions";

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http,Clock clock,JsonMapper jsonMapper,
        JwtAuthenticationConverter jwtAuthenticationConverter) {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            ErrorDto errorDto = new ErrorDto()
                                    .error("UNAUTHORIZED")
                                    .status(HttpServletResponse.SC_UNAUTHORIZED)
                                    .message("Missing or invalid access token")
                                    .timestamp(OffsetDateTime.now(clock))
                                    .path(request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            jsonMapper.writeValue(response.getWriter(), errorDto);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            ErrorDto errorDto = new ErrorDto()
                                    .error("ACCESS_DENIED")
                                    .status(HttpServletResponse.SC_FORBIDDEN)
                                    .message("You do not have permissions to perform this operation.")
                                    .timestamp(OffsetDateTime.now(clock))
                                    .path(request.getRequestURI());
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            jsonMapper.writeValue(response.getWriter(), errorDto);
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/public/**").permitAll()
                        .requestMatchers("/scalar/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthConverter() {
        JwtGrantedAuthoritiesConverter authConverter =
                new JwtGrantedAuthoritiesConverter();

        authConverter.setAuthoritiesClaimName(PERMISSIONS_CLAIM);
        authConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(
                authConverter
        );

        return converter;
    }

    @Bean
    JwtDecoder jwtDecoder(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuerUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
            String jwkSetUri,
            @Value("${spring.security.oauth2.resourceserver.jwt.audiences[0]}")
            String audience
    ) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
                .validateType(false).build();

        decoder.setJwtValidator(JwtValidators.createAtJwtValidator()
                .issuer(issuerUri).audience(audience).build());

        return decoder;
    }
}
