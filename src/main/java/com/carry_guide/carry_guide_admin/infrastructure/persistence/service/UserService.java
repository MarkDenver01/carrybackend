package com.carry_guide.carry_guide_admin.infrastructure.persistence.service;

import com.carry_guide.carry_guide_admin.domain.repository.*;
import com.carry_guide.carry_guide_admin.domain.service.UserDomainService;
import com.carry_guide.carry_guide_admin.infrastructure.persistence.entity.User;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService implements UserDomainService {
    @Value("${base.url.react}")
    String baseUrl;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    JpaRoleRepository roleRepository;

    @Autowired
    JpaAdminRepository adminRepository;

    @Autowired
    JpaDriverRepository driverRepository;

    @Autowired
    JpaCustomerRepository customerRepository;

    @Autowired
    EmailService emailService;

    @Autowired
    OtpCacheService otpCacheService;

    @Autowired
    SmsService smsService;

    @Autowired
    JwtUtils jwtUtils;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void requestOtp(String mobileNumber) throws Exception {
        String otp = generateOtp();
        otpCacheService.put(mobileNumber, otp);
        smsService.sendOtp(mobileNumber, otp);
    }

    @Override
    public boolean verifyOtp(String mobileNumber, String otp) throws Exception {
        String saved = otpCacheService.get(mobileNumber);
        if (saved == null || !saved.equals(otp)) { return  false; }
        otpCacheService.invalidate(mobileNumber);
        return true;
    }

    private String generateOtp() {
        int n = new Random().nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
