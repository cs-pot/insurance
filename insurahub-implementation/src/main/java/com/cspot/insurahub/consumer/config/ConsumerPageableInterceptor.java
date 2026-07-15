package com.cspot.insurahub.consumer.config;

import com.cspot.insurahub.consumer.validation.ConsumerPageableRequestValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
class ConsumerPageableInterceptor implements HandlerInterceptor {

    private final ConsumerPageableRequestValidator validator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.GET.matches(request.getMethod())) {
            validator.validate(request.getParameter("page"), request.getParameter("size"),
                    request.getParameterValues("sort"));
        }
        return true;
    }
}
