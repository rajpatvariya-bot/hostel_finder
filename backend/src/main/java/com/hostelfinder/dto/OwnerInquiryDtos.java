package com.hostelfinder.dto;

public class OwnerInquiryDtos {
  public record OwnerInquiryItem(
    Long id,
    Long hostelId,
    String hostelName,
    String status,
    int requestedRoomCount,
    String message
  ) {}

  public record DecisionRequest(String ownerNote) {}
}

