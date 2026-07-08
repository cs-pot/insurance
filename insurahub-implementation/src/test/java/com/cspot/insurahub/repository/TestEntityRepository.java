package com.cspot.insurahub.repository;

import com.cspot.insurahub.entity.TestEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestEntityRepository extends JpaRepository<TestEntity, Long> {
    Optional<TestEntity> findByName(String name);
}
