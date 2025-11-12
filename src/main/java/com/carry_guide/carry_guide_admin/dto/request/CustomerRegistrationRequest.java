package com.carry_guide.carry_guide_admin.dto.request;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerRegistrationRequest {
    @NotBlank
    private String userName;

    @NotBlank
    private String address;

    @Email
    private String email;        // required so we can generate a normal JWT (subject=email)

    private String photoUrl;
    private AccountStatus accountStatus; // optional; you can default
    private LocalDateTime createdDate;   // optional; can default to now
    
}

