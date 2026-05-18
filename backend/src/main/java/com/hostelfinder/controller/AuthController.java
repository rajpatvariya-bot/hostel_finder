package com.hostelfinder.controller;

import com.hostelfinder.dto.AuthDtos;
import com.hostelfinder.entity.HostelOwner;
import com.hostelfinder.entity.User;
import com.hostelfinder.entity.OtpVerification;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.HostelOwnerRepository;
import com.hostelfinder.repository.UserRepository;
import com.hostelfinder.repository.OtpVerificationRepository;
import com.hostelfinder.security.JwtTokenService;
import com.hostelfinder.service.OtpService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenService jwtTokenService;
  private final HostelOwnerRepository hostelOwnerRepository;
  private final OtpService otpService;
  private final OtpVerificationRepository otpRepo;

  public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                        JwtTokenService jwtTokenService, HostelOwnerRepository hostelOwnerRepository,
                        OtpService otpService, OtpVerificationRepository otpRepo) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtTokenService = jwtTokenService;
    this.hostelOwnerRepository = hostelOwnerRepository;
    this.otpService = otpService;
    this.otpRepo = otpRepo;
  }

  @PostMapping("/send-otp")
  public ResponseEntity<?> sendOtp(@Valid @RequestBody AuthDtos.SendOtpRequest req) {
      String otp = otpService.generateAndSendOtp(req.identifier());
      return ResponseEntity.ok(Map.of(
          "message", "OTP sent successfully",
          "dev_otp", otp // For local testing so you can see it in browser network tab
      ));
  }

  @PostMapping("/verify-otp")
  public ResponseEntity<?> verifyOtp(@Valid @RequestBody AuthDtos.VerifyOtpRequest req) {
      otpService.verifyOtp(req.identifier(), req.otpCode());
      
      // If user exists, login
      Optional<User> userOpt = userRepository.findByEmail(req.identifier().trim().toLowerCase());
      if (userOpt.isEmpty()) {
          // Check phone
          userOpt = userRepository.findByPhone(req.identifier().trim());
      }

      if (userOpt.isPresent()) {
          User u = userOpt.get();
          if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User is blocked");
          }
          String token = jwtTokenService.createAccessToken(u.getId(), u.getRole());
          return ResponseEntity.ok(new AuthDtos.LoginResponse(token, u.getRole()));
      }

      // User does not exist, return flag to proceed to signup
      return ResponseEntity.ok(Map.of(
          "message", "OTP Verified. Proceed to signup.",
          "verified", true,
          "requiresSignup", true
      ));
  }

  // Backward compatible login
  @PostMapping("/login")
  public AuthDtos.LoginResponse login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    User u = userRepository.findByEmail(req.email().trim().toLowerCase())
      .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

    if (!"ACTIVE".equalsIgnoreCase(u.getStatus())) {
      throw new ApiException(HttpStatus.FORBIDDEN, "User is blocked");
    }

    if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    String token = jwtTokenService.createAccessToken(u.getId(), u.getRole());
    return new AuthDtos.LoginResponse(token, u.getRole());
  }

  private boolean isVerifiedOtp(String identifier) {
      if (identifier == null || identifier.isBlank()) return false;
      String id = identifier.trim().toLowerCase();
      Optional<OtpVerification> otpOpt = otpRepo.findTopByIdentifierOrderByCreatedAtDesc(id);
      return otpOpt.isPresent() && Boolean.TRUE.equals(otpOpt.get().getIsVerified());
  }

  private void requireVerifiedEmailOrPhone(String email, String phone) {
      boolean emailVerified = isVerifiedOtp(email);
      boolean phoneVerified = isVerifiedOtp(phone);
      if (!emailVerified && !phoneVerified) {
          throw new ApiException(HttpStatus.BAD_REQUEST, "Either Email or Phone must be verified via OTP.");
      }
  }

  @PostMapping("/register/student")
  public AuthDtos.LoginResponse registerStudent(@Valid @RequestBody AuthDtos.StudentRegisterRequest req) {
    if (userRepository.findByEmail(req.email().trim().toLowerCase()).isPresent()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Email already in use");
    }
    
    requireVerifiedEmailOrPhone(req.email(), req.phone());

    User u = new User();
    u.setRole("STUDENT");
    u.setName(req.name());
    u.setEmail(req.email().trim().toLowerCase());
    u.setPhone(req.phone());
    u.setPasswordHash(passwordEncoder.encode(req.password()));
    u.setStatus("ACTIVE");

    userRepository.save(u);

    String token = jwtTokenService.createAccessToken(u.getId(), u.getRole());
    return new AuthDtos.LoginResponse(token, u.getRole());
  }

  @PostMapping("/register/owner")
  public AuthDtos.LoginResponse registerOwner(@Valid @RequestBody AuthDtos.OwnerRegisterRequest req) {
    if (userRepository.findByEmail(req.email().trim().toLowerCase()).isPresent()) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Email already in use");
    }
    if (userRepository.existsByPhone(req.phone())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Phone number already registered");
    }

    requireVerifiedEmailOrPhone(req.email(), req.phone());

    User u = new User();
    u.setRole("OWNER");
    u.setName(req.name());
    u.setEmail(req.email().trim().toLowerCase());
    u.setPhone(req.phone());
    u.setPasswordHash(passwordEncoder.encode(req.password()));
    u.setStatus("ACTIVE");

    u = userRepository.save(u);

    HostelOwner ho = new HostelOwner();
    ho.setUserId(u.getId());
    ho.setOwnerStatus("PENDING_VERIFICATION");
    hostelOwnerRepository.save(ho);

    String token = jwtTokenService.createAccessToken(u.getId(), u.getRole());
    return new AuthDtos.LoginResponse(token, u.getRole());
  }

  @GetMapping("/me")
  public Map<String, Object> me(@AuthenticationPrincipal User user) {
    Map<String, Object> res = new java.util.HashMap<>();
    res.put("id", user.getId());
    res.put("name", user.getName());
    res.put("email", user.getEmail());
    res.put("role", user.getRole());
    
    if ("OWNER".equalsIgnoreCase(user.getRole())) {
        hostelOwnerRepository.findById(user.getId()).ifPresent(ho -> {
            res.put("ownerStatus", ho.getOwnerStatus());
        });
    }
    return res;
  }
}
