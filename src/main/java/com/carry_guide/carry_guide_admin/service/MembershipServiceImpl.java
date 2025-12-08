package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.membership.MembershipResponse;
import com.carry_guide.carry_guide_admin.model.enums.MembershipStatus;
import com.carry_guide.carry_guide_admin.dto.membership.MemberRowDTO;
import com.carry_guide.carry_guide_admin.dto.membership.MembershipDashboardResponse;
import com.carry_guide.carry_guide_admin.dto.membership.TopMemberDTO;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.Membership;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MembershipServiceImpl implements MembershipService {

    private final JpaMembershipRepository membershipRepository;
    private final JpaCustomerRepository customerRepository;

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Manila");

    // ---------------------------------------------------------
    // 1. AVAIL MEMBERSHIP / RENEW MEMBERSHIP
    // ---------------------------------------------------------
    @Override
    @Transactional
    public Membership availMembershipForCustomer(Long customerId) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + customerId));

        // Look for existing membership; if none, create new
        Membership membership = membershipRepository.findByCustomer_CustomerId(customerId)
                .orElse(Membership.builder()
                        .customer(customer)
                        .pointsBalance(0)
                        .build()
                );

        // ⭐ Null safety (important for old records)
        if (membership.getPointsBalance() == null) {
            membership.setPointsBalance(0);
        }

        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate expiry = today.plusMonths(1);

        membership.setStartDate(today);
        membership.setExpiryDate(expiry);
        membership.setStatus(MembershipStatus.ACTIVE);

        return membershipRepository.save(membership);
    }

    // ---------------------------------------------------------
    // 2. ADD POINTS AFTER ORDER DELIVERY
    // ---------------------------------------------------------
    @Override
    @Transactional
    public void addPointsForCustomer(Long customerId, int pointsToAdd) {

        if (pointsToAdd <= 0) return;

        Membership membership = membershipRepository.findByCustomer_CustomerId(customerId)
                .orElseThrow(() ->
                        new IllegalStateException("Customer has no membership: " + customerId)
                );

        int currentPoints = membership.getPointsBalance() == null
                ? 0
                : membership.getPointsBalance();

        membership.setPointsBalance(currentPoints + pointsToAdd);

        membershipRepository.save(membership);
    }

    // ---------------------------------------------------------
    // 3. FETCH DASHBOARD DATA (NO AUTO-REFRESH HERE)
    // ---------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public MembershipDashboardResponse getDashboardOverview() {

        long total = membershipRepository.count();
        long active = membershipRepository.countByStatus(MembershipStatus.ACTIVE);
        long expiringSoon = membershipRepository.countByStatus(MembershipStatus.EXPIRING_SOON);
        long inactive = membershipRepository.countByStatus(MembershipStatus.INACTIVE);

        // New members this month
        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDate firstDay = today.with(TemporalAdjusters.firstDayOfMonth());
        long newThisMonth = membershipRepository.countNewMembersSince(firstDay);

        // Expiring this month
        LocalDate lastDay = today.with(TemporalAdjusters.lastDayOfMonth());
        long expiringThisMonth = membershipRepository.countExpiringBetween(firstDay, lastDay);

        // Table rows for Members List
        List<MemberRowDTO> memberRows = membershipRepository.findAllWithCustomer()
                .stream()
                .map(m -> MemberRowDTO.builder()
                        .name(m.getCustomer().getUserName())
                        .start(m.getStartDate().toString())
                        .expiry(m.getExpiryDate().toString())
                        .points(m.getPointsBalance())
                        .status(m.getStatus().name().replace("_", " "))
                        .build()
                ).toList();

        // Top 3 by points
        List<TopMemberDTO> topMembers = membershipRepository.findTopMembers(PageRequest.of(0, 3))
                .stream()
                .map(m -> TopMemberDTO.builder()
                        .name(m.getCustomer().getUserName())
                        .points(m.getPointsBalance())
                        .expiry(m.getExpiryDate().toString())
                        .build()
                ).toList();

        return MembershipDashboardResponse.builder()
                .active(active)
                .expiringSoon(expiringSoon)
                .inactive(inactive)
                .totalMembers(total)
                .newThisMonth(newThisMonth)
                .expiringThisMonth(expiringThisMonth)
                .topMembers(topMembers)
                .members(memberRows)
                .build();
    }

    // ---------------------------------------------------------
    // 4. STATUS REFRESH — CALLED BY SCHEDULER DAILY
    // ---------------------------------------------------------
    @Override
    @Transactional
    public void refreshAllMembershipStatuses() {

        List<Membership> memberships = membershipRepository.findAll();
        LocalDate today = LocalDate.now(ZONE_ID);

        for (Membership m : memberships) {

            LocalDate expiry = m.getExpiryDate();
            if (expiry == null) continue;

            long daysLeft = ChronoUnit.DAYS.between(today, expiry);

            MembershipStatus newStatus;

            if (daysLeft < 0) {
                newStatus = MembershipStatus.INACTIVE;
            } else if (daysLeft <= 30) {
                newStatus = MembershipStatus.EXPIRING_SOON;
            } else {
                newStatus = MembershipStatus.ACTIVE;
            }

            m.setStatus(newStatus);
        }

        membershipRepository.saveAll(memberships);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipResponse getMembershipByCustomerId(Long customerId) {

        Membership m = membershipRepository.findByCustomer_CustomerId(customerId)
                .orElseThrow(() ->
                        new IllegalStateException("Customer has no membership: " + customerId)
                );

        return MembershipResponse.builder()
                .membershipId(m.getMembershipId())
                .customerId(m.getCustomer().getCustomerId())
                .customerName(m.getCustomer().getUserName())
                .customerPhoto(m.getCustomer().getPhotoUrl())
                .startDate(m.getStartDate().toString())
                .expiryDate(m.getExpiryDate().toString())
                .pointsBalance(m.getPointsBalance())
                .status(m.getStatus().name())
                .build();
    }
}
