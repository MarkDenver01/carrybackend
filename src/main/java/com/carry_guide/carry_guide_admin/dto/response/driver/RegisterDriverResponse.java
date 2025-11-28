package com.carry_guide.carry_guide_admin.dto.response.driver;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterDriverResponse {

    private Long riderId;

    private String name;
    private String email;
    private String contact;
    private String address;

    private String driversLicenseNumber;

    private String photoUrl;
    private String frontIdUrl;
    private String backIdUrl;

    private String status;
}
