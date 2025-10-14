package com.carry_guide.carry_guide_admin.domain.model.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
    private String role;
    private String email;
    @Size(min = 11, max = 13, message = "Mobile number must be 11 digits")
    private int mobileNumber;
    private String driversLicenseNumber;
    private String frontIdUrl;
    private String backIdUrl;
}
