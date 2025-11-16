package com.carry_guide.carry_guide_admin.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GmailMobileRequest {
    private String mobileOrEmail;
    private String otp;
}
