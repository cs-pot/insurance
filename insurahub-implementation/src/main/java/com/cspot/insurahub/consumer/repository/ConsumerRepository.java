package com.cspot.insurahub.consumer.repository;

import com.cspot.insurahub.consumer.entity.Consumer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
    @Query("SELECT c FROM Consumer c WHERE "
            + "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) "
            + "OR c.personalId LIKE CONCAT('%', :search, '%')")
    Page<Consumer> findBySearch(@Param("search") String search, Pageable pageable);
}
