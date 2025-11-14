package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.domain.enums.RoleState;
import com.carry_guide.carry_guide_admin.dto.ProfileMappers;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.Driver;
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

        // 1) OTP verification
        if (!otpService.verifyOtp(mobileNumber, otpCode)) {
            throw new RuntimeException("OTP verification failed.");
        }

        // 2) Fetch user (if existing)
        User user = userRepository.findByMobileNumber(mobileNumber).orElse(null);

        boolean isCustomer = false;
        boolean isDriver = false;

        // =============================
        // CASE 3.1 → NEW USER SIGNUP
        // =============================
        if (user == null) {

            // Find or create CUSTOMER role
            Role role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseGet(() -> {
                        Role newRole = new Role();
                        newRole.setRoleState(RoleState.CUSTOMER);
                        return roleRepository.save(newRole);
                    });

            // Create new user
            user = new User();
            user.setMobileNumber(mobileNumber);
            user.setSignupMethod("MOBILE_OTP");
            user.setVerified(true);
            user.setRole(role);
            user = userRepository.save(user);

            // Create customer profile
            Customer newCustomer = new Customer();
            newCustomer.setUser(user);
            newCustomer.setUserName("");
            newCustomer.setMobileNumber(mobileNumber);
            newCustomer.setEmail("");
            newCustomer.setAddress("");
            newCustomer.setCreatedDate(LocalDateTime.now());
            newCustomer.setUserAccountStatus(AccountStatus.ACTIVATE);

            customerRepository.save(newCustomer);
        }

        // Determine role
        isCustomer = user.getRole().getRoleState() == RoleState.CUSTOMER;
        isDriver   = user.getRole().getRoleState() == RoleState.DRIVER;

        // 4) Generate JWT
        JwtUtils.JwtResponse jwt = jwtUtils.generateMobileToken(mobileNumber);

        LoginResponse response = new LoginResponse();
        response.setUserId(user.getUserId());
        response.setJwtToken(jwt.token());
        response.setJwtIssuedAt(jwt.issuedAt().toString());
        response.setJwtExpirationTime(jwt.expiresAt().toString());
        response.setRole(user.getRole().getRoleState().name());
        response.setUserName(user.getUserName());

        // ========================================
        // 5) Attach profile (FIXED MAJOR BUG)
        // ========================================
        if (isCustomer) {
            Customer customer = customerRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Customer profile not found!"));
            response.setCustomerResponse(ProfileMappers.toCustomerResponse(user, customer));
        }

        if (isDriver) {
            Driver driver = driverRepository.findByUser(user)
                    .orElseThrow(() -> new RuntimeException("Driver profile not found!"));
            response.setDriverResponse(ProfileMappers.toDriverResponse(user, driver));
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
