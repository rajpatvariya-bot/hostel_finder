package com.hostelfinder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
  public record LoginRequest(
    @Email @NotBlank String email,
    @NotBlank String password
  ) {}

  public record LoginResponse(
    String accessToken,
    String role
  ) {}

  public record StudentRegisterRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String phone,
    @NotBlank String password,
    String gender,
    Integer age
  ) {}

  public record OwnerRegisterRequest(
    @NotBlank String name,
    @Email @NotBlank String email,
    @NotBlank String phone,
    @NotBlank String password
  ) {}
  public record SendOtpRequest(
    @NotBlank String identifier
  ) {}

  public record VerifyOtpRequest(
    @NotBlank String identifier,
    @NotBlank String otpCode
  ) {}
}
