package com.hostelfinder.repository;

import com.hostelfinder.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByIdentifierOrderByCreatedAtDesc(String identifier);
}
