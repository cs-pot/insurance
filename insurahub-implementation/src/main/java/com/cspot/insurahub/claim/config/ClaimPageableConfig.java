package com.cspot.insurahub.claim.config;

import com.cspot.insurahub.api.ClaimsApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ClaimPageableConfig implements WebMvcConfigurer {

    private final ClaimPageableInterceptor claimPageableInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(claimPageableInterceptor)
                .addPathPatterns(ClaimsApi.PATH_GET_CLAIMS);
    }
}