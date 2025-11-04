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
    private String email;
    private String mobileNumber;
    private String roleState;
    private String photoUrl;
    private String address;
    private String driversLicenseNumber;
    private String frontIdUrl;
    private String backIdUrl;
    private String createdDate;
    private String accountStatus;
}
