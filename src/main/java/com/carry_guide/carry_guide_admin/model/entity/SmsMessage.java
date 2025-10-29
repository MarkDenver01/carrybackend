package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sms_messages")
public class SmsMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long smsId;
    private String mobileNumber;
    private String message;
    private String status;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;
}
