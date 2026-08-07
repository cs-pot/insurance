package com.cspot.insurahub.claim.filter;

import com.cspot.insurahub.claim.entity.Claim;
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
}
