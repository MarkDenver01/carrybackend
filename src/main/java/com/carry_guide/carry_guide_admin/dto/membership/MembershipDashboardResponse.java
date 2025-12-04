package com.carry_guide.carry_guide_admin.dto.membership;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MembershipDashboardResponse {

    private long active;
    private long expiringSoon;
    private long inactive;
    private long totalMembers;

    private long newThisMonth;
    private long expiringThisMonth;

    private List<TopMemberDTO> topMembers;
    private List<MemberRowDTO> members;
}
