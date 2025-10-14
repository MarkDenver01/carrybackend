package com.carry_guide.carry_guide_admin.domain.model.request;

import lombok.Data;

@Data
public class VerifyRequest {
    private String mobileNumber;
    private String otp;
}
