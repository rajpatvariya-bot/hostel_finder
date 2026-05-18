package com.hostelfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OwnerRegisterDtos {
  public record OwnerRegisterResponse(
    Long userId, 
    String ownerStatus,
    String accessToken,
    String role
  ) {}

  public record OwnerRegisterFields(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String phone,
    @NotBlank String password,
    @NotBlank String docType
  ) {}
}

