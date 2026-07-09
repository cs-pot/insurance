package com.cspot.insurahub.consumer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SoftDelete;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "consumers")
@SoftDelete(columnName = "deleted")
public class Consumer {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Setter(AccessLevel.PROTECTED)
    private UUID id;

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
