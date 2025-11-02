package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.model.entity.OtpVerification;
import com.carry_guide.carry_guide_admin.repository.JpaOtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    JpaOtpRepository otpRepository;

    @Autowired
    SemaphoreSmsService smsService;

    public void sendOtp(String mobileNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        smsService.sendOtp(mobileNumber, otp);

        OtpVerification entity = new OtpVerification();
        entity.setMobileNumber(mobileNumber);
        entity.setOtpCode(otp);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(entity);
    }

    public boolean verifyOtp(String mobileNumber, String otpCode) {
        OtpVerification record = otpRepository
                .findTopByMobileNumberOrderByCreatedAtDesc(mobileNumber)
                .orElseThrow(() -> new RuntimeException("OTP not found"));

        if (record.isVerified() || record.getExpiresAt().isBefore(LocalDateTime.now()))
            return false;

        if (record.getOtpCode().equals(otpCode)) {
            record.setVerified(true);
            otpRepository.save(record);
            return true;
        }
        return false;
    }
}
