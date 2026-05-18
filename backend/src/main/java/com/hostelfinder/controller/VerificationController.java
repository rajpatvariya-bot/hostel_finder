package com.hostelfinder.controller;

import com.hostelfinder.entity.OwnerDocument;
import com.hostelfinder.entity.User;
import com.hostelfinder.repository.OwnerDocumentRepository;
import com.hostelfinder.storage.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/verification")
public class VerificationController {
    private final OwnerDocumentRepository ownerDocRepo;
    private final FileStorageService storageService;

    public VerificationController(OwnerDocumentRepository ownerDocRepo, FileStorageService storageService) {
        this.ownerDocRepo = ownerDocRepo;
        this.storageService = storageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadAadhaar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (!"OWNER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can upload verification documents");
        }

        FileStorageService.StoredFile storedFile = storageService.save(file, "aadhaar");

        OwnerDocument doc = new OwnerDocument();
        doc.setOwnerUserId(user.getId());
        doc.setDocType("AADHAR");
        doc.setFileUrl(storedFile.url());
        doc.setFileMime(storedFile.mime());
        doc.setFileSizeBytes(storedFile.sizeBytes());
        doc.setAdminStatus("UPLOADED");

        ownerDocRepo.save(doc);

        return ResponseEntity.ok(Map.of("message", "Document uploaded successfully", "url", doc.getFileUrl()));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal User user) {
        if (!"OWNER".equalsIgnoreCase(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owners can check status here");
        }

        List<OwnerDocument> docs = ownerDocRepo.findByOwnerUserId(user.getId());
        if (docs.isEmpty()) {
            return ResponseEntity.ok(Map.of("status", "NO_DOCUMENT_UPLOADED"));
        }

        // Just returning the latest document's status
        OwnerDocument latestDoc = docs.get(docs.size() - 1);
        return ResponseEntity.ok(Map.of(
            "status", latestDoc.getAdminStatus(),
            "note", latestDoc.getAdminNote() != null ? latestDoc.getAdminNote() : "",
            "documentUrl", latestDoc.getFileUrl()
        ));
    }
}
