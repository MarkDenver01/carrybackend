package com.carry_guide.carry_guide_admin.model.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;
    private BigDecimal unitPrice;
}
