package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.RoleState;
import com.carry_guide.carry_guide_admin.dto.ProfileMappers;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
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

    public LoginResponse verifyOtpAndGenerateToken(String mobileNumber, String otpCode) {
        boolean verified = otpService.verifyOtp(mobileNumber, otpCode);
        if (!verified) {
            throw new RuntimeException("OTP verification failed");
        }

        // Load user if exists
        User user = userRepository.findByMobileNumber(mobileNumber).orElse(null);

        // Load customer if exists
        Customer customer = (user != null) ? customerRepository.findByUser(user).orElse(null) : null;

        Role role;

        // CASE 1: USER EXISTS BUT CUSTOMER RECORD NOT FOUND → FORCE CUSTOMER ROLE
        if (user != null && customer == null) {
            role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));
            user.setRole(role);
            userRepository.save(user);
        }
        // CASE 2: NEW USER SIGNUP → AUTO-ASSIGN CUSTOMER ROLE
        else if (user == null) {
            role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));

            user = new User();
            user.setMobileNumber(mobileNumber);
            user.setSignupMethod("MOBILE_OTP");
            user.setVerified(true);
            user.setRole(role);

            user = userRepository.save(user);
        }
        // CASE 3: USER EXISTS AND HAS CUSTOMER PROFILE → USE EXISTING ROLE
        else {
            if (user.getRole() == null) {
                // fallback safety
                role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));
                user.setRole(role);
                userRepository.save(user);
            } else {
                role = user.getRole();
            }
        }

        // Generate JWT
        JwtUtils.JwtResponse jwtToken = jwtUtils.generateMobileToken(mobileNumber);

        LoginResponse response = new LoginResponse();
        response.setJwtToken(jwtToken.token());
        response.setJwtIssuedAt(jwtToken.issuedAt().toString());
        response.setJwtExpirationTime(jwtToken.expiresAt().toString());
        response.setRole(role.getRoleState().name());
        response.setUserName(user.getUserName());
        final User finalUser = user;

        // CUSTOMER RESPONSE
        if (role.getRoleState() == RoleState.CUSTOMER) {
            customerRepository.findByUser(user).ifPresentOrElse(
                    c -> response.setCustomerResponse(ProfileMappers.toCustomerResponse(finalUser, c)),
                    () -> {
                        CustomerResponse temp = new CustomerResponse();
                        temp.setMobileNumber(mobileNumber);
                        response.setCustomerResponse(temp);
                    }
            );
        }

        // DRIVER RESPONSE
        if (role.getRoleState() == RoleState.DRIVER) {
            driverRepository.findByUser(user).ifPresentOrElse(
                    driver -> response.setDriverResponse(ProfileMappers.toDriverResponse(finalUser, driver)),
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
