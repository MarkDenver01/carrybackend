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

        // 3) Load customer or driver if user exists
        Customer customer = (user != null) ? customerRepository.findByUser(user).orElse(null) : null;
        Driver driver = (user != null) ? driverRepository.findByUser(user).orElse(null) : null;

        Role role;

        // =============================
        // CASE 3.1 → New User Signup
        // =============================
        if (user == null) {
            role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));

            user = new User();
            user.setMobileNumber(mobileNumber);
            user.setSignupMethod("MOBILE_OTP");
            user.setVerified(true);
            user.setRole(role);
            user = userRepository.save(user);

            // CREATE CUSTOMER PROFILE
            Customer newCustomer = new Customer();
            newCustomer.setUser(user);
            newCustomer.setUserName("");
            newCustomer.setMobileNumber(mobileNumber);
            newCustomer.setEmail("");
            newCustomer.setAddress("");
            newCustomer.setCreatedDate(LocalDateTime.now());
            newCustomer.setUserAccountStatus(AccountStatus.ACTIVATE);

            customerRepository.save(newCustomer);
            customer = newCustomer;
        }

        // =============================
        // CASE 3.2 → User exists but no customer (role must become CUSTOMER)
        // =============================
        else if (customer == null && driver == null) {
            // force customer role
            role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Default role CUSTOMER not found"));

            user.setRole(role);
            userRepository.save(user);

            // create missing customer profile
            Customer newCustomer = new Customer();
            newCustomer.setUser(user);
            newCustomer.setUserName(user.getUserName());
            newCustomer.setMobileNumber(mobileNumber);
            newCustomer.setEmail(user.getEmail());
            newCustomer.setAddress("");
            newCustomer.setCreatedDate(LocalDateTime.now());
            newCustomer.setUserAccountStatus(AccountStatus.ACTIVATE);

            customerRepository.save(newCustomer);
            customer = newCustomer;
        }

        // =============================
        // CASE 3.3 → User exists + customer exists
        // =============================
        else if (customer != null) {
            role = roleRepository.findRoleByRoleState(RoleState.CUSTOMER)
                    .orElseThrow(() -> new RuntimeException("Role CUSTOMER not found"));
            user.setRole(role);
            userRepository.save(user); // ensure role is correct
        }

        // =============================
        // CASE 3.4 → User is DRIVER
        // =============================
        else if (driver != null) {
            role = roleRepository.findRoleByRoleState(RoleState.DRIVER)
                    .orElseThrow(() -> new RuntimeException("Role DRIVER not found"));
            user.setRole(role);
            userRepository.save(user);
        }

        else {
            throw new RuntimeException("User role state is invalid.");
        }


        // 4) Generate JWT
        JwtUtils.JwtResponse jwt = jwtUtils.generateMobileToken(mobileNumber);

        LoginResponse response = new LoginResponse();
        response.setJwtToken(jwt.token());
        response.setJwtIssuedAt(jwt.issuedAt().toString());
        response.setJwtExpirationTime(jwt.expiresAt().toString());
        response.setRole(user.getRole().getRoleState().name());
        response.setUserName(user.getUserName());

        // 5) Attach customer or driver profile
        if (customer != null) {
            response.setCustomerResponse(ProfileMappers.toCustomerResponse(user, customer));
        }

        if (driver != null) {
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
