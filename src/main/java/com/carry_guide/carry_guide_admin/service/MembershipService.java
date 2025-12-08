package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.membership.MembershipDashboardResponse;
import com.carry_guide.carry_guide_admin.dto.membership.MembershipResponse;
import com.carry_guide.carry_guide_admin.model.entity.Membership;

public interface MembershipService {

    Membership availMembershipForCustomer(Long customerId);

    void addPointsForCustomer(Long customerId, int pointsToAdd);

    MembershipDashboardResponse getDashboardOverview();

    void refreshAllMembershipStatuses();

    MembershipResponse getMembershipByCustomerId(Long customerId);
}