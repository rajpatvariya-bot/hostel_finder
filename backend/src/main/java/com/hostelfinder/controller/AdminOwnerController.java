package com.hostelfinder.controller;

import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/owners")
public class AdminOwnerController {

  @PersistenceContext
  private EntityManager em;

  @GetMapping
  public List<Map<String, Object>> list(@RequestParam(defaultValue = "PENDING_VERIFICATION") String status, @AuthenticationPrincipal User user) {
    if (!"ADMIN".equalsIgnoreCase(user.getRole())) throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");

    // Using native query to avoid creating extra entities right now (college MVP).
    @SuppressWarnings("unchecked")
    List<Object[]> rows = em.createNativeQuery("""
        SELECT u.id, u.name, u.email, u.phone, ho.owner_status,
               (SELECT od.file_url FROM owner_documents od
                WHERE od.owner_user_id = u.id AND od.doc_type = 'AADHAR'
                ORDER BY od.submitted_at DESC LIMIT 1) AS aadhaar_url
        FROM users u
        JOIN hostel_owners ho ON ho.user_id = u.id
        WHERE ho.owner_status = :status
        ORDER BY u.id DESC
      """)
      .setParameter("status", status)
      .getResultList();

    return rows.stream().map(r -> {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("ownerUserId", ((Number) r[0]).longValue());
      m.put("name", r[1]);
      m.put("email", r[2]);
      m.put("phone", r[3]);
      m.put("ownerStatus", r[4]);
      m.put("aadhaarUrl", r[5] != null ? r[5] : null);
      return m;
    }).toList();
  }

  @PostMapping("/{ownerUserId}/approve")
  @org.springframework.transaction.annotation.Transactional
  public void approve(@PathVariable long ownerUserId, @AuthenticationPrincipal User user) {
    setStatus(ownerUserId, "VERIFIED", user);
  }

  @PostMapping("/{ownerUserId}/reject")
  @org.springframework.transaction.annotation.Transactional
  public void reject(@PathVariable long ownerUserId, @AuthenticationPrincipal User user) {
    setStatus(ownerUserId, "REJECTED", user);
  }

  @PostMapping("/{ownerUserId}/block")
  @org.springframework.transaction.annotation.Transactional
  public void block(@PathVariable long ownerUserId, @AuthenticationPrincipal User user) {
    setStatus(ownerUserId, "BLOCKED", user);
  }

  private void setStatus(long ownerUserId, String newStatus, User user) {
    if (!"ADMIN".equalsIgnoreCase(user.getRole())) throw new ApiException(HttpStatus.FORBIDDEN, "Admin only");

    int updated = em.createNativeQuery("""
        UPDATE hostel_owners
        SET owner_status = :st,
            verified_at = CASE WHEN :st='VERIFIED' THEN CURRENT_TIMESTAMP ELSE verified_at END
        WHERE user_id = :id
      """)
      .setParameter("st", newStatus)
      .setParameter("id", ownerUserId)
      .executeUpdate();

    if (updated == 0) throw new ApiException(HttpStatus.NOT_FOUND, "Owner not found");
  }
}

