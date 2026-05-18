package com.hostelfinder.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class InquiryDtos {
  public record CreateInquiryRequest(
    @NotNull Long hostelId,
    @Min(1) int requestedRoomCount,
    String message,
    String requestType
  ) {}

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
    String ownerNote
  ) {}

  public record UpdateInquiryStatusRequest(
    String status,
    String ownerNote
  ) {}
}

