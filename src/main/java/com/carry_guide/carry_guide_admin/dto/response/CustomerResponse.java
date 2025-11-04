package com.carry_guide.carry_guide_admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
    private Long customerId;
    private String userName;
    private String email;
    private String mobileNumber;
    private String roleState;
    private String photoUrl;
    private String address;
    private String createdDate;
    private String accountStatus;
}
