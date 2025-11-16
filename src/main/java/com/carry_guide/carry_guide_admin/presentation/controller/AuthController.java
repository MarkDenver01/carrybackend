package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.LoginRequest;
import com.carry_guide.carry_guide_admin.dto.request.GmailMobileRequest;
import com.carry_guide.carry_guide_admin.dto.response.AdminResponse;
import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.repository.JpaGmailTokenRepository;
import com.carry_guide.carry_guide_admin.service.CustomizedUserDetails;
import com.carry_guide.carry_guide_admin.service.UserService;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.utils.DateTimeHelper;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/user")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    UserService userService;

    @Autowired
    JpaGmailTokenRepository gmailTokenRepository;

    @Value("${gmail.redirect.uri}")
    private String redirectUri;


    @Value("${GMAIL_CLIENT_SECRET_JSON}")
    private String clientSecretJson;

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

        JwtUtils.JwtResponse jwtToken = jwtUtils.generateToken(customizedUserDetails);

        String role = customizedUserDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse(null);

        Optional<User> optionalUser = userService.findByEmail(customizedUserDetails.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found", "status", false));
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
                        String.valueOf(user.getAdmin().getCreatedDate()),
                        user.getAdmin().getProfileUrl(),
                        String.valueOf(user.getAdmin().getAccountStatus())
                );
            }

            loginResponse = new LoginResponse(
                    jwtToken.token(),
                    DateTimeHelper.format(jwtToken.issuedAt()),
                    DateTimeHelper.format(jwtToken.expiresAt()),
                    user.getUserName(),
                    role, adminResponse);
        }

        return ResponseEntity.ok(loginResponse);
    }

    @PostMapping("/public/send_otp")
    public ResponseEntity<?> sendOtp(@RequestBody GmailMobileRequest gmailMobileRequest) {
        userService.sendOtp(gmailMobileRequest.getMobileOrEmail());
        return ResponseEntity.ok("OTP successfully sent to " + gmailMobileRequest.getMobileOrEmail());
    }

    @PostMapping("/public/verify_otp")
    public ResponseEntity<?> verifyOtp(@RequestBody GmailMobileRequest gmailMobileRequest) {
        try {
            LoginResponse response = userService.verifyOtpAndGenerateToken(
                    gmailMobileRequest.getMobileOrEmail(),
                    gmailMobileRequest.getOtp()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage(), "status", false));
        }
    }

    @GetMapping("/auth")
    public void authorize(HttpServletResponse response) throws Exception {
        var flow = buildFlow();
        var url = flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .build();

        response.sendRedirect(url);
    }

    private GoogleAuthorizationCodeFlow buildFlow() throws Exception {
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
                GsonFactory.getDefaultInstance(),
                new StringReader(clientSecretJson)
        );

        return new GoogleAuthorizationCodeFlow.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                clientSecrets,
                List.of(GmailScopes.GMAIL_SEND, GmailScopes.GMAIL_READONLY)
        )
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }
}
