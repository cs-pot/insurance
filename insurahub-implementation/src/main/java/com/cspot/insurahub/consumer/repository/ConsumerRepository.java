package com.cspot.insurahub.consumer.repository;

import com.cspot.insurahub.consumer.entity.Consumer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ConsumerRepository extends JpaRepository<Consumer, UUID> {
}
