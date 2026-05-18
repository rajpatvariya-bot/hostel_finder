package com.hostelfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "hostel_owners")
public class HostelOwner {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "owner_status", nullable = false)
    private String ownerStatus; // PENDING_VERIFICATION, VERIFIED, REJECTED, BLOCKED

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getOwnerStatus() { return ownerStatus; }
    public void setOwnerStatus(String ownerStatus) { this.ownerStatus = ownerStatus; }

    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(LocalDateTime verifiedAt) { this.verifiedAt = verifiedAt; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
