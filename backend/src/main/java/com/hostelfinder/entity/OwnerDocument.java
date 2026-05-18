package com.hostelfinder.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "owner_documents")
public class OwnerDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "document_id")
    private Long documentId;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "doc_type", nullable = false)
    private String docType; // 'AADHAR','PAN','DRIVING_LICENSE','OTHER'

    @Column(name = "file_url", nullable = false, length = 600)
    private String fileUrl;

    @Column(name = "file_mime", length = 100)
    private String fileMime;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "admin_status", nullable = false)
    private String adminStatus; // 'UPLOADED','APPROVED','REJECTED'

    @Column(name = "admin_note", length = 400)
    private String adminNote;

    @Column(name = "submitted_at", insertable = false, updatable = false)
    private LocalDateTime submittedAt;

    // Getters and Setters
    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public String getDocType() { return docType; }
    public void setDocType(String docType) { this.docType = docType; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getFileMime() { return fileMime; }
    public void setFileMime(String fileMime) { this.fileMime = fileMime; }

    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }

    public String getAdminStatus() { return adminStatus; }
    public void setAdminStatus(String adminStatus) { this.adminStatus = adminStatus; }

    public String getAdminNote() { return adminNote; }
    public void setAdminNote(String adminNote) { this.adminNote = adminNote; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
