package com.cspot.insurahub.insurancepackage.filter;

import com.cspot.insurahub.insurancepackage.entity.InsurancePackage;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class PackageSpecification {

    private PackageSpecification() {
    }

    public static Specification<InsurancePackage> byFilter(PackageFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            String name = filter.normalizedName();

            if (name != null) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase(Locale.ROOT) + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
