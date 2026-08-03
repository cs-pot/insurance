package com.cspot.insurahub.claim.entity;

import com.cspot.insurahub.common.ImmutableAuditableEntity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "receipts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Receipt extends ImmutableAuditableEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "claim_id", nullable = false, updatable = false)
    private Claim claim;

    @Column(name = "file_name", nullable = false, updatable = false)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, updatable = false, length = 100)
    private String contentType;

    @Column(name = "size_bytes", nullable = false, updatable = false)
    private Long sizeBytes;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "content", nullable = false, updatable = false)
    private byte[] content;

    public Receipt(
            Claim claim,
            String fileName,
            String contentType,
            Long sizeBytes,
            byte[] content
    ) {
        this.claim = claim;
        this.originalFileName = fileName;
        this.contentType = contentType;
        this.sizeBytes = sizeBytes;
        this.content = content;

        if (claim != null) {
            claim.setReceipt(this);
        }
    }
}
