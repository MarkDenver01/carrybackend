package com.carry_guide.carry_guide_admin.jwt.controller;

import com.carry_guide.carry_guide_admin.jwt.model.entity.Role;
import com.carry_guide.carry_guide_admin.jwt.model.entity.User;
import com.carry_guide.carry_guide_admin.jwt.model.state.RoleState;
import com.carry_guide.carry_guide_admin.jwt.repository.RoleRepository;
import com.carry_guide.carry_guide_admin.jwt.repository.UserRepository;
import com.carry_guide.carry_guide_admin.jwt.security.request.LoginRequest;
import com.carry_guide.carry_guide_admin.jwt.security.request.RegisterRequest;
import com.carry_guide.carry_guide_admin.jwt.security.response.LoginResponse;
import com.carry_guide.carry_guide_admin.jwt.security.response.MessageResponse;
import com.carry_guide.carry_guide_admin.jwt.security.service.CustomizedUserDetails;
import com.carry_guide.carry_guide_admin.jwt.service.UserService;
import com.carry_guide.carry_guide_admin.jwt.util.JwtUtils;
import com.carry_guide.carry_guide_admin.utils.AuthUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserService userService;

    @Autowired
    AuthUtil authUtil;

    @PostMapping("/public/user_login")
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

        // set the authentication
        SecurityContextHolder.getContext().setAuthentication(authentication);

        CustomizedUserDetails customizedUserDetails = (CustomizedUserDetails) authentication.getPrincipal();

        String jwtToken = jwtUtils.generateToken(customizedUserDetails);

        // collect roles from customized user details
        List<String> roles = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // prepare the response body, now including the JWT token directly in the body
        LoginResponse loginResponse = new LoginResponse(
                customizedUserDetails.getUsername(),
                roles,
                jwtToken);

        // return the response entity with JWT token included in the response body
        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/public/user_register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Email already exists"));
        }

        // create a new user account
        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()));

        Set<String> tempRoles = registerRequest.getRoles();
        Role role;

        if (tempRoles == null || tempRoles.isEmpty()) {
            role = roleRepository.findRoleByRoleState(RoleState.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("No role found"));
        } else {
            String userRoles = tempRoles.iterator().next();
            if (userRoles.equals("admin")) {
                role = roleRepository.findRoleByRoleState(RoleState.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("No role found"));
            } else {
                role = roleRepository.findRoleByRoleState(RoleState.ROLE_USER)
                        .orElseThrow(() -> new RuntimeException("No role found"));
            }

            user.setSignupMethod("email");
        }
        user.setRole(role);
        userRepository.save(user);
        return ResponseEntity.ok(new MessageResponse("User registered successfully"));
    }

    @GetMapping("/get_user_email")
    public String getCurrentUserEmail(@AuthenticationPrincipal UserDetails userDetails) {
        return (userDetails != null ? userDetails.getUsername() : "");
    }

    @PostMapping("/public/forgot_password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            userService.generatePasswordResetToken(email);
            return ResponseEntity.ok(new MessageResponse("Password reset token generated successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Internal Server Error"));
        }
    }

    @PostMapping("/public/reset_password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,
                                           @RequestParam String newPassword) {
        try {
            userService.resetPassword(token, newPassword);
            return ResponseEntity.ok(new MessageResponse("Password reset token updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse(e.getMessage()));
        }
    }

}
