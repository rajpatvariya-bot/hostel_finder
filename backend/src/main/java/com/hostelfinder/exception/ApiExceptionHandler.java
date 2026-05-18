package com.hostelfinder.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", 400);
    body.put("message", "Validation failed");

    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fields.put(fe.getField(), fe.getDefaultMessage());
    }
    body.put("fieldErrors", fields);
    return ResponseEntity.badRequest().body(body);
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<?> handleApi(ApiException ex) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", ex.getStatus().value());
    body.put("message", ex.getMessage());
    return ResponseEntity.status(ex.getStatus()).body(body);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<?> handleOther(Exception ex) {
    System.err.println("=== UNEXPECTED ERROR ===");
    ex.printStackTrace();
    System.err.println("Error Message: " + ex.getMessage());
    System.err.println("Error Class: " + ex.getClass().getName());
    System.err.println("=== END ERROR ===");
    
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", 500);
    body.put("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error");
    body.put("error", ex.getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
  }
}
