package com.cspot.insurahub.enrollment.service;

import com.cspot.insurahub.consumer.service.IdpIdMappingService;
import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.enrollment.entity.EnrollmentStatus;
import com.cspot.insurahub.enrollment.mapper.EnrollmentMapper;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.model.EnrollmentResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final IdpIdMappingService idpIdMappingService;
    private final EnrollmentMapper enrollmentMapper;

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollments(EnrollmentStatus status) {
        UUID consumerId = idpIdMappingService.getCurrentAuthenticatedConsumerId();
        Specification<Enrollment> spec = buildEnrollmentSpecification(consumerId, status);
        List<Enrollment> enrollments = enrollmentRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "createdAt"));
        return enrollmentMapper.toResponseList(enrollments);
    }

    private Specification<Enrollment> buildEnrollmentSpecification(UUID consumerId, EnrollmentStatus status) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(criteriaBuilder.equal(root.get("consumer").get("id"), consumerId));
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
