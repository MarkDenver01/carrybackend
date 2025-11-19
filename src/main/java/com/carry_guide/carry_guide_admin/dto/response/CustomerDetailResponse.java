package com.carry_guide.carry_guide_admin.dto.response;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import lombok.Data;

@Data
public class CustomerDetailResponse {
    private Long customerId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String photoUrl;
    private String address;

    private String createdDate;
    private AccountStatus accountStatus;
}
