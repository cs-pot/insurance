package com.cspot.insurahub.consumer.config;

import com.cspot.insurahub.common.validation.PageableRequestValidator;
import com.cspot.insurahub.consumer.enumeration.ConsumerSortProperty;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
class ConsumerPageableInterceptor implements HandlerInterceptor {

    private static final List<String> ALLOWED_SORT_PROPERTIES = ConsumerSortProperty.propertyNames();

    private final PageableRequestValidator validator;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (HttpMethod.GET.matches(request.getMethod())) {
            validator.validate(request.getParameter("page"), request.getParameter("size"),
                    request.getParameterValues("sort"), ALLOWED_SORT_PROPERTIES);
        }
        return true;
    }
}
