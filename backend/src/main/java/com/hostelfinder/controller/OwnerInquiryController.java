package com.hostelfinder.controller;

import com.hostelfinder.dto.InquiryDtos;
import com.hostelfinder.entity.Inquiry;
import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.InquiryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/inquiries")
public class OwnerInquiryController {
  private final InquiryRepository inquiryRepository;

  public OwnerInquiryController(InquiryRepository inquiryRepository) {
    this.inquiryRepository = inquiryRepository;
  }

  @GetMapping
  public List<InquiryDtos.InquiryItem> getMyInquiries(@AuthenticationPrincipal User user) {
    if (!"OWNER".equalsIgnoreCase(user.getRole())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Only owners can view this");
    }

    return inquiryRepository.findByHostel_OwnerUserIdOrderByIdDesc(user.getId()).stream()
        .map(i -> new InquiryDtos.InquiryItem(
            i.getId(),
            i.getHostel().getId(),
            i.getHostel().getHostelName(),
            i.getHostel().getArea().getName(),
            i.getRequestType(),
            i.getStatus(),
            i.getRequestedRoomCount(),
            i.getMessage(),
            i.getStudent().getName(),
            i.getStudent().getPhone(),
            i.getOwnerNote()))
        .toList();
  }

  @PatchMapping("/{id}/status")
  public void updateStatus(@AuthenticationPrincipal User user, @PathVariable Long id, 
                           @RequestBody InquiryDtos.UpdateInquiryStatusRequest req) {
    Inquiry inq = inquiryRepository.findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inquiry not found"));

    if (!inq.getHostel().getOwnerUserId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Not your inquiry");
    }

    inq.setStatus(req.status());
    if (req.ownerNote() != null) inq.setOwnerNote(req.ownerNote());
    inquiryRepository.save(inq);
  }
}
