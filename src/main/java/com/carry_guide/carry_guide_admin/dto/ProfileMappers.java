package com.carry_guide.carry_guide_admin.dto;

import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.dto.response.DriverResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.Driver;
import com.carry_guide.carry_guide_admin.model.entity.User;

import java.time.format.DateTimeFormatter;

public class ProfileMappers {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public static CustomerResponse toCustomerResponse(User u, Customer c) {
        return new CustomerResponse(
                c.getCustomerId(),
                c.getUserName(),
                c.getEmail(),
                c.getMobileNumber(),
                u.getRole() != null ? u.getRole().getRoleState().name() : "CUSTOMER",
                c.getPhotoUrl(),
                c.getAddress(),
                c.getCreatedDate() != null ? TS.format(c.getCreatedDate()) : null,
                c.getUserAccountStatus() != null ? c.getUserAccountStatus().name() : null
        );
    }

    public static DriverResponse toDriverResponse(User u, Driver d) {
        return new DriverResponse(
                d.getDriverId(),
                d.getUserName(),
                d.getEmail(),
                d.getMobileNumber(),
                u.getRole() != null ? u.getRole().getRoleState().name() : "DRIVER",
                d.getPhotoUrl(),
                d.getAddress(),
                d.getDriversLicenseNumber(),
                d.getFrontIdUrl(),
                d.getBackIdUrl(),
                d.getCreatedDate() != null ? TS.format(d.getCreatedDate()) : null,
                d.getUserAccountStatus() != null ? d.getUserAccountStatus().name() : null
        );
    }



}
