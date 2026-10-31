package com.hostelfinder.controller;

import com.hostelfinder.dto.InquiryDtos;
import com.hostelfinder.entity.Hostel;
import com.hostelfinder.entity.Inquiry;
import com.hostelfinder.entity.HostelRoomType;
import com.hostelfinder.entity.User;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.HostelRepository;
import com.hostelfinder.repository.InquiryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/inquiries")
public class StudentInquiryController {
  private final HostelRepository hostelRepository;
  private final InquiryRepository inquiryRepository;
  private static final String REQUEST_TYPE_INQUIRY = "INQUIRY";
  private static final String REQUEST_TYPE_BOOKING = "BOOKING";

  public StudentInquiryController(HostelRepository hostelRepository, InquiryRepository inquiryRepository) {
    this.hostelRepository = hostelRepository;
    this.inquiryRepository = inquiryRepository;
  }

  @PostMapping
  public void create(@Valid @RequestBody InquiryDtos.CreateInquiryRequest req,
                     @AuthenticationPrincipal User user) {
    if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Only students can send inquiries");
    }

    Hostel h = hostelRepository.findByIdFull(req.hostelId())
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Hostel not found"));

    if (!"PUBLISHED".equalsIgnoreCase(h.getStatus())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Hostel not available");
    }

    HostelRoomType selectedRoomType = null;
    if (req.roomTypeId() != null) {
      selectedRoomType = h.getRoomTypes().stream()
        .filter(roomType -> roomType.getId() != null && roomType.getId().equals(req.roomTypeId()))
        .findFirst()
        .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Selected room type is invalid for this hostel"));
    }

    Inquiry inq = new Inquiry();
    inq.setHostel(h);
    inq.setRoomType(selectedRoomType);
    inq.setStudent(user);
    inq.setRequestedRoomCount(req.requestedRoomCount());
    inq.setMessage(req.message());
    inq.setRequestType(normalizeRequestType(req.requestType()));
    inq.setStatus("PENDING");
    inquiryRepository.save(inq);
  }

  @GetMapping
  public List<InquiryDtos.InquiryItem> mine(@AuthenticationPrincipal User user) {
    if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Only students can view this");
    }

    return inquiryRepository.findByStudentIdOrderByIdDesc(user.getId()).stream()
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
            i.getOwnerNote(),
            i.getRoomType() != null ? i.getRoomType().getId() : null,
            i.getRoomType() != null ? i.getRoomType().getRoomType() : null))
        .toList();
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
    if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "Only students can delete inquiries");
    }

    Inquiry inquiry = inquiryRepository.findById(id)
      .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Inquiry not found"));

    if (inquiry.getStudent() == null || !inquiry.getStudent().getId().equals(user.getId())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own inquiries");
    }

    inquiryRepository.delete(inquiry);
  }

  private String normalizeRequestType(String requestType) {
    if (requestType == null || requestType.isBlank()) {
      return REQUEST_TYPE_INQUIRY;
    }
    String normalized = requestType.trim().toUpperCase();
    if (!REQUEST_TYPE_INQUIRY.equals(normalized) && !REQUEST_TYPE_BOOKING.equals(normalized)) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid request type");
    }
    return normalized;
  }
}
