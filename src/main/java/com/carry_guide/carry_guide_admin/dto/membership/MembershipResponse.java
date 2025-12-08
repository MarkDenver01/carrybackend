package com.carry_guide.carry_guide_admin.dto.membership;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MembershipResponse {

    private Long membershipId;
    private Long customerId;

    private String customerName;
    private String customerPhoto;

    private String startDate;
    private String expiryDate;

    private Integer pointsBalance;
    private String status;
}
