package com.cspot.insurahub.consumer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import com.cspot.insurahub.common.SoftDeletableAuditableEntity;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "consumers")
public class Consumer extends SoftDeletableAuditableEntity {
    
    @Column(name = "idp_id", nullable = false)
    private String idpId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "personal_id", nullable = false)
    private String personalId;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "city", nullable = false)
    private String city;
}
