package com.carry_guide.carry_guide_admin.domain.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MobileLoginRequest {
    @Size(min = 11, max = 13, message = "Mobile number must be 11 digits")
    private String mobileNumber;
}
