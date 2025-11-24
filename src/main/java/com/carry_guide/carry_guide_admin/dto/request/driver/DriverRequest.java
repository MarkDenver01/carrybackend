package com.carry_guide.carry_guide_admin.dto.request.driver;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DriverRequest {

    private String userName;
    private String email;
    private String mobileNumber;
    private String address;
    private String driversLicenseNumber;

    private MultipartFile photoFile;
    private MultipartFile frontIdFile;
    private MultipartFile backIdFile;
}