package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.membership.MembershipDashboardResponse;
import com.carry_guide.carry_guide_admin.model.entity.Membership;
import com.carry_guide.carry_guide_admin.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/api/membership")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MembershipController {

    private final MembershipService membershipService;

    // Endpoint for Membership Overview React page
    @GetMapping("/dashboard")
    public MembershipDashboardResponse getDashboard() {
        return membershipService.getDashboardOverview();
    }

    // Called when mobile app avails membership
    @PostMapping("/customer/{customerId}/avail")
    public Membership availMembership(@PathVariable Long customerId) {
        return membershipService.availMembershipForCustomer(customerId);
    }

    // Called from order completion logic (or mobile if needed)
    @PostMapping("/customer/{customerId}/add-points")
    public void addPoints(
            @PathVariable Long customerId,
            @RequestParam("points") int points
    ) {
        membershipService.addPointsForCustomer(customerId, points);
    }
}