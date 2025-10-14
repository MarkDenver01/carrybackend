package com.carry_guide.carry_guide_admin.domain.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MobileLoginRequest {
    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 13, message = "Mobile number must be between 10 and 13 characters")
    @Pattern(regexp = "^[+0-9]+$", message = "Invalid mobile number format")
    private String mobileNumber;
}
