package com.hostelfinder.controller;

import com.hostelfinder.entity.OwnerDocument;
import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.OwnerDocumentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/verifications")
public class AdminVerificationController {

    @PersistenceContext
    private EntityManager em;
    
    private final OwnerDocumentRepository ownerDocRepo;

    public AdminVerificationController(OwnerDocumentRepository ownerDocRepo) {
        this.ownerDocRepo = ownerDocRepo;
    }

    @GetMapping
    public List<Map<String, Object>> listVerifications(@AuthenticationPrincipal User user) {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");

        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery("""
            SELECT d.document_id, d.owner_user_id, u.name, d.file_url, d.submitted_at, d.admin_status
            FROM owner_documents d
            JOIN users u ON u.id = d.owner_user_id
            ORDER BY d.submitted_at DESC
          """).getResultList();

        return rows.stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("documentId", ((Number) r[0]).longValue());
            m.put("ownerUserId", ((Number) r[1]).longValue());
            m.put("ownerName", r[2]);
            m.put("fileUrl", r[3]);
            m.put("submittedAt", r[4]);
            m.put("status", r[5]);
            return m;
        }).toList();
    }

    @PutMapping("/{id}")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> updateVerification(@PathVariable long id, @RequestBody Map<String, String> body, @AuthenticationPrincipal User user) {
        if (!"ADMIN".equalsIgnoreCase(user.getRole())) throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");

        String action = body.get("action"); // APPROVE or REJECT
        String newStatus = "APPROVE".equalsIgnoreCase(action) ? "APPROVED" : "REJECTED";
        String note = body.get("note");

        OwnerDocument doc = ownerDocRepo.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Document not found"));

        doc.setAdminStatus(newStatus);
        doc.setAdminNote(note);
        ownerDocRepo.save(doc);

        // Update the Hostel Owner's overall status
        String ownerStatus = "APPROVE".equalsIgnoreCase(action) ? "VERIFIED" : "REJECTED";
        em.createNativeQuery("""
            UPDATE hostel_owners
            SET owner_status = :st,
                verified_at = CASE WHEN :st='VERIFIED' THEN CURRENT_TIMESTAMP ELSE verified_at END,
                rejection_reason = :note
            WHERE user_id = :id
          """)
          .setParameter("st", ownerStatus)
          .setParameter("note", note)
          .setParameter("id", doc.getOwnerUserId())
          .executeUpdate();

        return ResponseEntity.ok(Map.of("message", "Verification updated successfully"));
    }
}
