package com.hostelfinder.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
public class HealthController {

  @GetMapping("/")
  public ResponseEntity<Map<String, Object>> root() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "service", "Hostel Finder Backend API",
        "timestamp", Instant.now().toString(),
        "endpoints", List.of(
            "/api/public/cities",
            "/api/public/hostels",
            "/api/auth/login",
            "/api/auth/register-student",
            "/api/auth/register-owner"
        )
    ));
  }

  @GetMapping("/health")
  public ResponseEntity<Map<String, Object>> health() {
    return ResponseEntity.ok(Map.of(
        "status", "UP",
        "timestamp", Instant.now().toString()
    ));
  }

  @GetMapping("/api/health")
  public ResponseEntity<Map<String, Object>> apiHealth() {
    return health();
  }
}
