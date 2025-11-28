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

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 20)
    private String contact;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RiderStatus status;

    private int ordersToday;
    private int completedDeliveries;
    private int workload; // ordersToday * 10

    private LocalDateTime lastAssigned;
    private LocalDateTime lastActive;
}
