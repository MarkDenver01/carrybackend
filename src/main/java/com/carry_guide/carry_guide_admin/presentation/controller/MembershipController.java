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
}