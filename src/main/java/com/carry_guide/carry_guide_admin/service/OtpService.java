package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.infrastructure.config.OtpCleanupConfig;
import com.carry_guide.carry_guide_admin.model.entity.OtpVerification;
import com.carry_guide.carry_guide_admin.repository.JpaOtpRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Service
public class OtpService {

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int OTP_RATE_LIMIT_SECONDS = 60;

    @Autowired
    private JpaOtpRepository otpRepository;

    @Autowired
    private SemaphoreSmsService smsService;

    @Autowired
    private OtpCleanupConfig otpCleanupConfig;

    /**
     * 📤 Send OTP and store in database with expiry.
     */
    public void sendOtp(String mobileNumber) {
        Optional<OtpVerification> lastOtp = otpRepository.findTopByMobileNumberOrderByCreatedAtDesc(mobileNumber);
        if (lastOtp.isPresent()) {
            LocalDateTime lastSent = lastOtp.get().getCreatedAt();
            if (lastSent != null && lastSent.isAfter(LocalDateTime.now().minusSeconds(OTP_RATE_LIMIT_SECONDS))) {
                throw new RuntimeException("Please wait at least " + OTP_RATE_LIMIT_SECONDS + " seconds before requesting another OTP.");
            }
        }

        String otp = String.format("%05d", new Random().nextInt(99999));
        smsService.sendOtp(mobileNumber, otp);

        OtpVerification entity = new OtpVerification();
        entity.setMobileNumber(mobileNumber);
        entity.setOtpCode(otp);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        entity.setVerified(false);
        otpRepository.save(entity);

        log.info("OTP {} sent to {}", otp, mobileNumber);
    }

    /**
     * ✅ Verify OTP if it matches, not expired, and not already used.
     */
    public boolean verifyOtp(String mobileNumber, String otpCode) {
        OtpVerification record = otpRepository
                .findTopByMobileNumberOrderByCreatedAtDesc(mobileNumber)
                .orElseThrow(() -> new RuntimeException("OTP not found. Please request a new one."));

        if (record.isVerified()) {
            log.warn("OTP already used for {}", mobileNumber);
            return false;
        }

        if (record.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("OTP expired for {}", mobileNumber);
            return false;
        }

        boolean match = record.getOtpCode().equals(otpCode);
        if (match) {
            record.setVerified(true);
            otpRepository.save(record);
            log.info("✅ OTP verified successfully for {}", mobileNumber);
            return true;
        } else {
            log.warn("❌ Invalid OTP {} entered for {}", otpCode, mobileNumber);
            return false;
        }
    }

    /**
     * 🧹 Smart scheduled cleanup for expired OTP records.
     * Skips DB delete if none are expired to reduce load.
     */
    @Scheduled(cron = "#{@otpCleanupConfig.cleanupCron}")
    public void cleanupExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        long count = otpRepository.countByExpiresAtBefore(now);

        if (count == 0) {
            log.debug("No expired OTP records found at {}", now);
            return; // Skip unnecessary delete query
        }

        int deleted = otpRepository.deleteByExpiresAtBefore(now);
        log.info("🧹 Cleaned up {} expired OTP records at {}", deleted, now);
    }
}
