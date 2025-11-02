package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long deliveryId;

    @OneToOne @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne @JoinColumn(name = "driver_id")
    private Driver driver;

    private LocalDateTime assignedAt;
    private LocalDateTime deliveredAt;
    private String deliveryStatus; // ASSIGNED, PICKED_UP, DELIVERED, FAILED
}
