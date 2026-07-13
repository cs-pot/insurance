package com.cspot.insurahub.config;

import com.cspot.insurahub.model.ErrorDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
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
    SecurityFilterChain apiSecurity(HttpSecurity http, Clock clock, JsonMapper jsonMapper,
                                    Converter<Jwt, ? extends AbstractAuthenticationToken> converter) {
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
                                    .status(401)
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
                                    .status(403)
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
                        oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(converter)))
                .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName(PERMISSIONS_CLAIM);
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter
        );

        return authenticationConverter;
    }
}
