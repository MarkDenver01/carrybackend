package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.Role;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

import static com.carry_guide.carry_guide_admin.utils.Utility.getRoleState;

@Service
public class UserService  {
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
    OtpService otpService;

    @Autowired
    JwtUtils jwtUtils;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new HashMap<>();

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public void sendOtp(String mobileNumber) {
        otpService.sendOtp(mobileNumber);
    }

    public LoginResponse verifyOtpAndGenerateToken(String mobileNumber, String otpCode, String userRole) {
        boolean verified = otpService.verifyOtp(mobileNumber, otpCode);
        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        Role role = roleRepository.findRoleByRoleState(getRoleState(userRole))
                .orElseThrow(() -> new RuntimeException("Invalid role"));

        User user = userRepository.findByMobileNumber(mobileNumber).orElseGet(() -> {
            User newUser = new User();
            newUser.setMobileNumber(mobileNumber);
            newUser.setSignupMethod("MOBILE_OTP");
            newUser.setVerified(true);
            newUser.setRole(role); // e.g. CUSTOMER or RIDER
           return userRepository.save(newUser);
        });

        // TODO
        return null;
    }


}
