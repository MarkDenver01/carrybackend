package com.carry_guide.carry_guide_admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyResponse {
    private boolean success;       // true if OTP valid
    private boolean userExists;    // true if user record found
    private String tempJwtToken;   // only when userExists == false (to allow register step)
    private Date tempIssuedAt;
    private Date tempExpiresAt;
}
