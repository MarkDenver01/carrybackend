package com.carry_guide.carry_guide_admin.infrastructure.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class OtpCleanupConfig {
    /**
     * Cron expression for OTP cleanup schedule.
     * Default: every hour ("0 0 * * * *")
     */
    @Value("${otp.cleanup.cron:0 0 * * * *}")
    private String cleanupCron;
}
