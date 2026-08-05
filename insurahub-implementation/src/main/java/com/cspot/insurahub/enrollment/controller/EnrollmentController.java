package com.cspot.insurahub.enrollment.controller;

import com.cspot.insurahub.api.EnrollmentsApi;
import com.cspot.insurahub.enrollment.entity.EnrollmentStatus;
import com.cspot.insurahub.enrollment.service.EnrollmentService;
import com.cspot.insurahub.model.EnrollmentResponse;
import com.cspot.insurahub.model.PostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class EnrollmentController implements EnrollmentsApi {

    private final EnrollmentService enrollmentService;

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('enroll:self')")
    public PostResponse createEnrollment(UUID planId) {
        return enrollmentService.enrollCurrentAuthenticatedConsumerOnPlan(planId);
    }

    @Override
    @PreAuthorize("hasAuthority('view:own:enrollments')")
    public List<EnrollmentResponse> getEnrollments(String status) {
        EnrollmentStatus entityStatus = status != null ? EnrollmentStatus.valueOf(status) : null;
        return enrollmentService.getEnrollments(entityStatus);
    }
}
