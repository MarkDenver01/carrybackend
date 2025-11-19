package com.carry_guide.carry_guide_admin.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerDetailRequest {
    @NotBlank
    private String userName;

    @Email
    private String email;

    private String mobileNumber;

    private String photoUrl;
    private String address;
}
