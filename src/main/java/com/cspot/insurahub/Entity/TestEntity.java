package com.cspot.insurahub.Entity;

import jakarta.persistence.*;

@Entity
@Table(name = "test_table")
public class TestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected TestEntity() {
    }

    public TestEntity(String name) {
        this.name = name;
    }


}
