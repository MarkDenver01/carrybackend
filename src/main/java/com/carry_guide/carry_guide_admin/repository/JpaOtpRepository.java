package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.OtpVerification;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface JpaOtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByMobileNumberOrderByCreatedAtDesc(String mobileNumber);

    Optional<OtpVerification> findTopByEmailAddressOrderByCreatedAtDesc(String email);

    Optional<OtpVerification> findByMobileNumberAndOtpCodeAndVerifiedFalse(String mobileNumber, String otpCode);
    @Transactional
    int deleteByExpiresAtBefore(LocalDateTime now);

    long countByExpiresAtBefore(LocalDateTime now);
}
