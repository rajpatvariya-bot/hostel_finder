package com.hostelfinder.repository;

import com.hostelfinder.entity.OwnerDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnerDocumentRepository extends JpaRepository<OwnerDocument, Long> {
    List<OwnerDocument> findByOwnerUserId(Long ownerUserId);
    List<OwnerDocument> findByAdminStatus(String adminStatus);
}
