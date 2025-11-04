package com.carry_guide.carry_guide_admin.dto.service;


import com.carry_guide.carry_guide_admin.dto.response.LoginResponse;
import com.carry_guide.carry_guide_admin.infrastructure.security.JwtUtils;
import com.carry_guide.carry_guide_admin.model.entity.User;

import java.util.Optional;

public interface UserDomainService {
    Optional<User> findByEmail(String email);
    void sendOtp(String mobileNumber);

    LoginResponse verifyOtpAndGenerateToken(String mobileNumber, String otpCode, String userRole);
}
