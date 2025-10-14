package com.carry_guide.carry_guide_admin.infrastructure.persistence.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class OtpCacheService {
    private Cache<String, OtpEntry> cache;


    @Value("${otp.expiration.minutes}")
    private int otpExpiryMinutes;

    @PostConstruct
    public void init() {
        cache = Caffeine.newBuilder()
                .expireAfterWrite(otpExpiryMinutes, TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build();
    }

    public void put(String mobileNumber, String code) {
        cache.put(mobileNumber, new OtpEntry(code, Instant.now().plusSeconds(otpExpiryMinutes * 60L)));
    }

    public String get(String mobileNumber) {
        OtpEntry e = cache.getIfPresent(mobileNumber);
        if (e == null) return null;
        if (Instant.now().isAfter(e.expiresAt)) {
            cache.invalidate(mobileNumber);
            return null;
        }
        return e.code;
    }

    public void invalidate(String mobileNumber) {
        cache.invalidate(mobileNumber);
    }

    private static class OtpEntry {
        final String code;
        final Instant expiresAt;
        OtpEntry(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }
}
