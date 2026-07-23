package com.cspot.insurahub.plan.config;

import com.cspot.insurahub.api.PlansApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class PlanPageableConfig implements WebMvcConfigurer {

    private final PlanPageableInterceptor planPageableInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(planPageableInterceptor)
                .addPathPatterns(PlansApi.PATH_GET_PACKAGE_PLANS);
    }
}
