package com.cspot.insurahub.consumer.config;

import com.cspot.insurahub.api.ConsumersApi;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ConsumerPageableConfig implements WebMvcConfigurer {

    private final ConsumerPageableInterceptor consumerPageableInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(consumerPageableInterceptor)
                .addPathPatterns(ConsumersApi.PATH_GET_CONSUMERS);
    }
}
