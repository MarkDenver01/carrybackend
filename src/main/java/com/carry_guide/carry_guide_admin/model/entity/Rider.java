package com.carry_guide.carry_guide_admin.model.entity;

import com.carry_guide.carry_guide_admin.domain.enums.RiderStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "riders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rider {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long riderId;

    // ===========================
    // DRIVER REGISTRATION FIELDS
    // ===========================
    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 20)
    private String contact;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 50)
    private String driversLicenseNumber;

    // Uploaded image URLs
    private String photoUrl;
    private String frontIdUrl;
    private String backIdUrl;

    private String homeBase;

    // ===========================
    // DELIVERY STATUS FIELDS
    // ===========================
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiderStatus status; // Available, On_Delivery, Offline

    private int ordersToday;

    private int completedDeliveries;

    private int workload;

    private LocalDateTime lastAssigned;

    private LocalDateTime lastActive;
}
