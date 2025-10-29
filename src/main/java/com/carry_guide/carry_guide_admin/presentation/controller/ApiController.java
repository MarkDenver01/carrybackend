package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.LoginRequest;
import com.carry_guide.carry_guide_admin.dto.request.MobileLoginRequest;
import com.carry_guide.carry_guide_admin.dto.request.VerifyRequest;
import com.carry_guide.carry_guide_admin.dto.response.AdminResponse;
import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.service.CustomizedUserDetails;
import com.carry_guide.carry_guide_admin.service.UserService;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class ApiController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    @PostMapping("/public/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword())
            );
        } catch (AuthenticationException e) {
            Map<String, Object> map = new HashMap<>();
            map.put("message", e.getMessage());
            map.put("status", false);
            return new ResponseEntity<>(map, HttpStatus.NOT_FOUND);
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomizedUserDetails customizedUserDetails = (CustomizedUserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateToken(customizedUserDetails);

        String role = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);

        Optional<User> optionalUser = userService.findByEmail(customizedUserDetails.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found", "st atus", false));
        }

        User user= optionalUser.get();
        LoginResponse loginResponse = null;


        if ("ADMIN".equals(role)) {
            AdminResponse adminResponse = null;
            if (user.getAdmin() != null) {
                adminResponse = new AdminResponse(
                        user.getAdmin().getAdminId(),
                        user.getAdmin().getUserName(),
                        user.getAdmin().getEmail(),
                        String.valueOf(user.getAdmin().getCreatedDate())
                );
            }

            loginResponse = new LoginResponse(
                    jwtToken, user.getUserName(), role, adminResponse);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/public/send_otp")
    public ResponseEntity<?> sendOtp(@RequestBody MobileLoginRequest mobileLoginRequest) {
        try {
            userService.requestOtp(mobileLoginRequest.getMobileNumber());
            return ResponseEntity.ok("Success");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("public/verify_otp")
    public ResponseEntity<?> verifyOtp(@RequestBody VerifyRequest verifyRequest)  {
        try {
            boolean success = userService.verifyOtp(
                    verifyRequest.getMobileNumber(),
                    verifyRequest.getOtp());

            if (success) {
                String jwtToken = jwtUtils.generateMobileToken(verifyRequest.getMobileNumber());
                return ResponseEntity.ok("Success");
            } else {
                return ResponseEntity.badRequest().body("Invalid or expired verification code");
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}
