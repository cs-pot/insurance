package com.cspot.insurahub.enrollment.service;

import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.consumer.repository.ConsumerRepository;
import com.cspot.insurahub.consumer.service.IdpIdMappingService;
import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.enrollment.entity.EnrollmentStatus;
import com.cspot.insurahub.enrollment.mapper.EnrollmentMapper;
import com.cspot.insurahub.enrollment.repository.EnrollmentRepository;
import com.cspot.insurahub.model.EnrollmentResponse;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import com.cspot.insurahub.model.PostResponse;
import com.cspot.insurahub.plan.entity.InsurancePlan;
import com.cspot.insurahub.plan.repository.InsurancePlanRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final IdpIdMappingService idpIdMappingService;
    private final ConsumerRepository consumerRepository;
    private final InsurancePlanRepository insurancePlanRepository;
    private final EnrollmentMapper enrollmentMapper;
    private final EnrollmentValidationService enrollmentValidationService;

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

    @Transactional
    public PostResponse enrollCurrentAuthenticatedConsumerOnPlan(UUID planId) {
        Consumer consumer = getConsumer();
        InsurancePlan insurancePlan = insurancePlanRepository.findByIdOrThrow(planId);
        enrollmentValidationService.assertConsumerCanEnrollOnPlan(consumer, insurancePlan);
        Enrollment enrollment = createEnrollment(consumer, insurancePlan);
        log.info("Enrolled consumer with ID {} on plan with ID {}", consumer.getId(), insurancePlan.getId());
        return new PostResponse(enrollment.getId());
    }

    private @NonNull Enrollment createEnrollment(Consumer consumer, InsurancePlan insurancePlan) {
        Enrollment enrollment = new Enrollment(consumer, insurancePlan);
        enrollment = enrollmentRepository.save(enrollment);
        return enrollment;
    }

    private @NonNull Consumer getConsumer() {
        UUID consumerId = idpIdMappingService.getCurrentAuthenticatedConsumerId();
        Consumer consumer = consumerRepository.findByIdOrThrow(consumerId);
        return consumer;
    }
}
