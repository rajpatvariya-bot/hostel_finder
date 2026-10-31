package com.hostelfinder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InquiryDtos {
  public record CreateInquiryRequest(
    @NotNull Long hostelId,
    @Min(1) int requestedRoomCount,
    String message,
    String requestType,
    Long roomTypeId
  ) {
    public CreateInquiryRequest(Long hostelId, int requestedRoomCount, String message, String requestType) {
      this(hostelId, requestedRoomCount, message, requestType, null);
    }
  }

  public record InquiryItem(
    Long id,
    Long hostelId,
    String hostelName,
    String areaName,
    String requestType,
    String status,
    int requestedRoomCount,
    String message,
    String studentName,
    String studentPhone,
    String ownerNote,
    Long roomTypeId,
    String roomType
  ) {}

  public record UpdateInquiryStatusRequest(
    String status,
    String ownerNote
  ) {}
}

