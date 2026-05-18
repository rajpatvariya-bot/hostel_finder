package com.hostelfinder.service;

import com.hostelfinder.entity.OtpVerification;
import com.hostelfinder.exception.ApiException;
import com.hostelfinder.repository.OtpVerificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {
    private final OtpVerificationRepository otpRepo;
    private final NotificationService notificationService;
    private static final int MAX_ATTEMPTS = 5;

    public OtpService(OtpVerificationRepository otpRepo, NotificationService notificationService) {
        this.otpRepo = otpRepo;
        this.notificationService = notificationService;
    }

    public String generateAndSendOtp(String identifier) {
        identifier = identifier.trim().toLowerCase();
        
        // Prevent spam
        Optional<OtpVerification> lastOtpOpt = otpRepo.findTopByIdentifierOrderByCreatedAtDesc(identifier);
        if (lastOtpOpt.isPresent()) {
            OtpVerification lastOtp = lastOtpOpt.get();
            if (lastOtp.getCreatedAt() != null && lastOtp.getCreatedAt().plusSeconds(30).isAfter(LocalDateTime.now())) {
                throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Please wait 30 seconds before requesting a new OTP.");
            }
        }

        String otpCode = String.format("%06d", new Random().nextInt(999999));
        
        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setIdentifier(identifier);
        otpVerification.setOtpCode(otpCode); // In a real app we might hash this, but simplified here
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpVerification.setIsVerified(false);
        otpRepo.save(otpVerification);

        notificationService.sendOtp(identifier, otpCode);
        return otpCode;
    }

    public void verifyOtp(String identifier, String otpCode) {
        identifier = identifier.trim().toLowerCase();
        
        OtpVerification otpVerification = otpRepo.findTopByIdentifierOrderByCreatedAtDesc(identifier)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No OTP requested."));

        if (otpVerification.getIsVerified()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP already verified.");
        }

        if (otpVerification.getAttempts() >= MAX_ATTEMPTS) {
            throw new ApiException(HttpStatus.TOO_MANY_REQUESTS, "Maximum attempts reached. Request a new OTP.");
        }

        if (LocalDateTime.now().isAfter(otpVerification.getExpiryTime())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "OTP has expired.");
        }

        if (!otpVerification.getOtpCode().equals(otpCode.trim())) {
            otpVerification.setAttempts(otpVerification.getAttempts() + 1);
            otpRepo.save(otpVerification);
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid OTP code.");
        }

        otpVerification.setIsVerified(true);
        otpRepo.save(otpVerification);
    }
}
