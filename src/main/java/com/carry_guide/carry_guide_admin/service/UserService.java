package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.ProfileMappers;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
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
            throw new RuntimeException("OTP verification failed");
        }

        Role role = roleRepository.findRoleByRoleState(getRoleState(userRole))
                .orElseThrow(() -> new RuntimeException("Invalid role"));

        // Find or create user
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setMobileNumber(mobileNumber);
                    newUser.setSignupMethod("MOBILE_OTP");
                    newUser.setVerified(true);
                    newUser.setRole(role);
                    return userRepository.save(newUser);
                });

        JwtUtils.JwtResponse jwtToken = jwtUtils.generateMobileToken(mobileNumber);

        LoginResponse response = new LoginResponse();
        response.setJwtToken(jwtToken.token());
        response.setJwtIssuedAt(jwtToken.issuedAt().toString());
        response.setJwtExpirationTime(jwtToken.expiresAt().toString());
        response.setRole(role.getRoleState().name());
        response.setUserName(user.getUserName());

        if (role.getRoleState().name().equalsIgnoreCase("CUSTOMER")) {
            customerRepository.findByUser(user).ifPresentOrElse(
                    customer -> response.setCustomerResponse(ProfileMappers.toCustomerResponse(user, customer)),
                    () -> {
                        // If OTP verified but user not yet registered, allow registration flow
                        CustomerResponse temp = new CustomerResponse();
                        temp.setMobileNumber(mobileNumber);
                        response.setCustomerResponse(temp);
                    }
            );
        } else if (role.getRoleState().name().equalsIgnoreCase("DRIVER")) {
            driverRepository.findByUser(user).ifPresentOrElse(
                    driver -> response.setDriverResponse(ProfileMappers.toDriverResponse(user, driver)),
                    () -> {
                        throw new RuntimeException("Driver not found. Only in-house drivers can log in.");
                    }
            );
        }
        return response;
    }

    public Optional<User> findByMobileNumber(String mobileNumber) {
        return userRepository.findByMobileNumber(mobileNumber);
    }

    public boolean existsByMobileNumber(String mobileNumber) {
        return userRepository.findByMobileNumber(mobileNumber).isPresent();
    }
}
