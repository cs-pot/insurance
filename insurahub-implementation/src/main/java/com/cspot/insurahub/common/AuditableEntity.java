package com.cspot.insurahub.common;

import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Qudit base
 *
 * <p>Entities extending this must have created, updated, deleted columns <b>6 in total</b></p>
 */
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AuditableEntity extends SoftDeletableAuditableEntity {
}
