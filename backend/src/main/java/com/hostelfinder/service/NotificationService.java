package com.hostelfinder.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtp(String identifier, String otp) {
        String message = "Your Hostel Finder verification code is: " + otp + ". It is valid for 5 minutes.";
        
        System.out.println("====== OTP SENT TO " + identifier + " ======");
        System.out.println("OTP Code: " + otp);
        System.out.println("=========================================");

        if (identifier.contains("@")) {
            try {
                SimpleMailMessage email = new SimpleMailMessage();
                email.setTo(identifier);
                email.setSubject("Hostel Finder - Verification Code");
                email.setText(message);
                mailSender.send(email);
            } catch (Exception e) {
                System.err.println("Failed to send email. Ensure spring.mail properties are correct. " + e.getMessage());
            }
        }
        // Place for SMS logic in the future (Twilio/Fast2SMS)
    }
}
