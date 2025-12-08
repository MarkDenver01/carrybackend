package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_tokens")
@Data
public class NotificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;      // optional, depende sa system mo
    private String token;
    private String platform;      // "WEB" or "ANDROID"

    private LocalDateTime createdAt;

    public NotificationToken() {}

    public NotificationToken(Long customerId, String token, String platform) {
        this.customerId = customerId;
        this.token = token;
        this.platform = platform;
        this.createdAt = LocalDateTime.now();
    }
}
