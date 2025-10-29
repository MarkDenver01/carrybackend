package com.carry_guide.carry_guide_admin.dto.service;


import com.carry_guide.carry_guide_admin.model.entity.User;

import java.util.Optional;

public interface UserDomainService {
    Optional<User> findByEmail(String email);

    String requestOtp(String mobileNumber);

    boolean verifyOtp(String mobileNumber, String otp);
}
