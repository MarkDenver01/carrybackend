package com.carry_guide.carry_guide_admin.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String jwtToken;
    private String userName;
    private String role;
    private AdminResponse adminResponse;
    private CustomerResponse customerResponse;
    private DriverResponse driverResponse;

    // for admin
    public LoginResponse(String jwtToken, String userName, String role, AdminResponse adminResponse) {
        this.jwtToken = jwtToken;
        this.userName = userName;
        this.role = role;
        this.adminResponse = adminResponse;
    }

    // for customer
    public LoginResponse(String jwtToken, String userName, String role, CustomerResponse customerResponse) {
        this.jwtToken = jwtToken;
        this.userName = userName;
        this.role = role;
        this.customerResponse = customerResponse;
    }

    // for driver
    public LoginResponse(String jwtToken, String userName, String role, DriverResponse driverResponse) {
        this.jwtToken = jwtToken;
        this.userName = userName;
        this.role = role;
        this.driverResponse = driverResponse;
    }
}
