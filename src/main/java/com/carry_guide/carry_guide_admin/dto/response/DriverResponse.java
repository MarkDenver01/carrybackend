package com.carry_guide.carry_guide_admin.dto.response;

import com.carry_guide.carry_guide_admin.dto.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private Long driverId;
    private String userName;
    private String address;
    private int mobileNumber;
    private String email;
    private String driversLicenseNumber;
    private String frontIdUrl;
    private String backIdUrl;
    private String createdDate;
    private AccountStatus accountStatus;
}
