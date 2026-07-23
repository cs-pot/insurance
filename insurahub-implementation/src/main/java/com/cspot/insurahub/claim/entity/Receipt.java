package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.claim.enumeration.ReceiptStorageType;
import com.cspot.insurahub.common.ImmutableAuditableEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "receipts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receipt extends ImmutableAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, updatable = false)
    private Claim claim;

    @Column(name = "file_name", nullable = false, updatable = false)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, updatable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private Long sizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "storage_type", nullable = false, updatable = false, length = 32)
    private ReceiptStorageType storageType;

    @Column(name = "storage_key",
        nullable = false,
        unique = true,
        length = 512,
        updatable = false)
    private String storageKey;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", updatable = false)
    private byte[] content;

    public Receipt(
            Claim claim,
            String fileName,
            String contentType,
            Long sizeBytes,
            ReceiptStorageType storageType,
            String storageKey,
            byte[] content
    ) {
        this.claim = claim;
        this.originalFileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.storageType = storageType;
        this.storageKey = storageKey;
        this.content = content;
    }
}
