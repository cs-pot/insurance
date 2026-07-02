package com.cspot.insurahub.repository;

import com.cspot.insurahub.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}
