package com.cspot.insurahub.config.auth0;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth0")
public class Auth0ConfigurationProperties {
    private String domain;
    private String clientId;
    private String clientSecret;
}