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
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    SmsService smsService;

    @Autowired
    JwtUtils jwtUtils;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new HashMap<>();

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public String requestOtp(String mobileNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(mobileNumber, otp);
        otpExpiry.put(mobileNumber, LocalDateTime.now().plusMinutes(5));

        smsService.sendOtp(mobileNumber, otp);
        return "Verification code sent to: " + mobileNumber;
    }

    @Override
    public boolean verifyOtp(String mobileNumber, String otp){
        if (!otpStorage.containsKey(mobileNumber)) return false;

        if (LocalDateTime.now().isAfter(otpExpiry.get(mobileNumber))) {
            otpStorage.remove(mobileNumber);
            return false; // expired
        }


        if (otpStorage.get(mobileNumber).equals(otp)) {
            otpStorage.remove(mobileNumber);
            otpExpiry.remove(mobileNumber);

            // Create or verify user
            User user = userRepository.findByMobileNumber(mobileNumber)
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setMobileNumber(mobileNumber);
                        return newUser;
                    });
            user.setVerified(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
