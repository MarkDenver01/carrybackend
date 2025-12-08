package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.model.entity.Membership;
import com.carry_guide.carry_guide_admin.service.MembershipService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/public/api/membership")
@RequiredArgsConstructor
public class MembershipCustomerController {
    private final MembershipService membershipService;

    // ✅ ANDROID: POST /user/public/api/membership/customer/{id}/avail
    @PostMapping("/customer/{customerId}/avail")
    public ResponseEntity<Membership> availMembership(
            @PathVariable Long customerId
    ) {
        Membership membership = membershipService.availMembershipForCustomer(customerId);
        return ResponseEntity.ok(membership);
    }

    // ✅ ANDROID: GET /user/public/api/membership/customer/{id}
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Membership> getMyMembership(
            @PathVariable Long customerId
    ) {
        Membership membership = membershipService.getMembershipByCustomerId(customerId);
        return ResponseEntity.ok(membership);
    }

    // ✅ OPTIONAL: ADD POINTS
    @PostMapping("/customer/{customerId}/add-points")
    public ResponseEntity<Void> addPoints(
            @PathVariable Long customerId,
            @RequestParam("points") int points
    ) {
        membershipService.addPointsForCustomer(customerId, points);
        return ResponseEntity.ok().build();
    }
}
