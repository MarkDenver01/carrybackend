package com.carry_guide.carry_guide_admin.domain.model.request;

import lombok.Data;

@Data
public class LoginRequest {
    private String userName;
    private String password;
    private String role;
    private String email;
    private int mobileNumber;
    private String driversLicenseNumber;
    private String frontIdUrl;
    private String backIdUrl;
}
