package com.carry_guide.carry_guide_admin.dto;

import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toEntity(CustomerDetailRequest req) {
        Customer c = new Customer();
        c.setUserName(req.getUserName());
        c.setEmail(req.getEmail());
        c.setMobileNumber(req.getMobileNumber());
        c.setPhotoUrl(req.getPhotoUrl());
        c.setAddress(req.getAddress());
        return c;
    }

    public CustomerDetailResponse toResponse(Customer c) {
        CustomerDetailResponse res = new CustomerDetailResponse();
        res.setCustomerId(c.getCustomerId());
        res.setUserName(c.getUserName());
        res.setEmail(c.getEmail());
        res.setMobileNumber(c.getMobileNumber());
        res.setPhotoUrl(c.getPhotoUrl());
        res.setAddress(c.getAddress());

        res.setCreatedDate(c.getCreatedDate().toString());
        res.setAccountStatus(c.getUserAccountStatus());

        return res;
    }
}
