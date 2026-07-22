package com.cspot.insurahub.enrollment.repository;

import com.cspot.insurahub.enrollment.entity.Enrollment;
import com.cspot.insurahub.enrollment.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByConsumerIdAndStatusOrderByCreatedAtDesc(UUID consumerId, EnrollmentStatus status);
}
