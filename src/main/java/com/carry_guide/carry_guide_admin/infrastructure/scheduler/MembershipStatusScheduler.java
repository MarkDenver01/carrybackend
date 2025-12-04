package com.carry_guide.carry_guide_admin.infrastructure.scheduler;

import com.carry_guide.carry_guide_admin.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MembershipStatusScheduler {

    private final MembershipService membershipService;

    // Run daily at 2:05 AM Asia/Manila
    @Scheduled(cron = "0 5 2 * * *", zone = "Asia/Manila")
    public void refreshStatuses() {
        log.info("Running scheduled membership status refresh...");
        membershipService.refreshAllMembershipStatuses();
    }
}