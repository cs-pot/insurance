package com.cspot.insurahub.claim.filter;

import com.cspot.insurahub.claim.entity.Claim;
import com.cspot.insurahub.consumer.entity.Consumer;
import com.cspot.insurahub.enrollment.entity.Enrollment;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class ClaimSpecification {

    private ClaimSpecification() {
    }

    public static Specification<Claim> withDetails() {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                root.fetch("enrollment").fetch("consumer");
                root.fetch("enrollment").fetch("plan");
            }
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Claim> byConsumerId(UUID consumerId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("enrollment").get("consumer").get("id"), consumerId);
    }

    public static Specification<Claim> claimNumberContains(String claimNumber) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(root.get("claimNumber"), "%" + claimNumber + "%");
    }

    public static Specification<Claim> consumerFullNameContains(String consumer) {
        return (root, query, criteriaBuilder) -> {
            Join<Claim, Enrollment> enrollmentJoin = root.join("enrollment");
            Join<Enrollment, Consumer> consumerJoin = enrollmentJoin.join("consumer");
            Expression<String> fullName = criteriaBuilder.concat(
                    criteriaBuilder.concat(consumerJoin.get("firstName"), " "),
                    consumerJoin.get("lastName")
            );
            return criteriaBuilder.like(fullName, "%" + consumer + "%");
        };
    }
}
