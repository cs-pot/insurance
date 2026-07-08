package com.cspot.insurahub;

import com.cspot.insurahub.entity.TestEntity;
import com.cspot.insurahub.repository.TestEntityRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Transactional
public class DatabaseIntegrationTest extends BaseIntegrationTest {
    @Autowired
    private TestEntityRepository repository;

    @Test
    void contextLoads() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void entityCreation() {
        repository.save(new TestEntity("John"));
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void entityUpdate() {
        repository.save(new TestEntity("John"));
        TestEntity entity = repository.findByName("John")
                .orElseThrow(() -> new AssertionError("Entity must exist"));

        entity.setName("Jane");
        repository.saveAndFlush(entity);

        Optional<TestEntity> updatedEntity = repository.findById(entity.getId());

        assertThat(updatedEntity).isPresent();
        assertThat(updatedEntity.get().getName()).isEqualTo("Jane");
    }
}
