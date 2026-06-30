package com.cspot.insurahub.Repository;

import com.cspot.insurahub.Entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
}
