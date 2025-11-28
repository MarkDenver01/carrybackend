package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.cancelorder.CancelOrderRequest;
import com.carry_guide.carry_guide_admin.dto.request.cancelorder.DeliverOrderRequest;

import com.carry_guide.carry_guide_admin.domain.enums.OrderStatus;
import com.carry_guide.carry_guide_admin.domain.enums.PaymentMethod;
import com.carry_guide.carry_guide_admin.dto.request.CheckoutRequest;
import com.carry_guide.carry_guide_admin.dto.request.OrderItemRequest;
import com.carry_guide.carry_guide_admin.dto.response.OrderItemResponse;
import com.carry_guide.carry_guide_admin.dto.response.OrderResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.Order;
import com.carry_guide.carry_guide_admin.model.entity.OrderItem;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaOrderRepository;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor

public class OrderService {

    private final JpaOrderRepository orderRepository;
    private final JpaProductRepository productRepository;
    private final JpaCustomerRepository customerRepository;

    @Transactional
    public OrderResponse checkout(CheckoutRequest request) {

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        Order order = new Order();
        order.setCustomer(customer);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setDeliveryAddress(
                request.getDeliveryAddress() != null ? request.getDeliveryAddress()
                        : customer.getAddress()   // default customer address
        );
        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.getProductId()));

            // ⭐ Get latest price from ProductPrice table
            BigDecimal unitPrice = product.getLatestPrice();

            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(item);
        }

        order.setItems(orderItems);
        order.setSubtotal(subtotal);

        BigDecimal deliveryFee = request.getDeliveryFee() != null ? request.getDeliveryFee() : BigDecimal.ZERO;
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;

        BigDecimal total = subtotal.add(deliveryFee).subtract(discount);

        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotalAmount(total);

        // ⭐ WALLET PAYMENT
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {

            if (customer.getWalletBalance().compareTo(total) < 0) {
                throw new IllegalStateException("Insufficient wallet balance");
            }

            customer.setWalletBalance(customer.getWalletBalance().subtract(total));
            customerRepository.save(customer);
        }

        Order saved = orderRepository.save(order);

        return mapToResponse(saved);
    }



    @Transactional
    public List<OrderResponse> getOrdersForCustomer(Long customerId) {
        return orderRepository.findByCustomer_CustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public OrderResponse getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));
        return mapToResponse(order);
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(oi -> OrderItemResponse.builder()
                        .productId(oi.getProduct().getProductId())
                        .productName(oi.getProduct().getProductName())
                        .productImgUrl(oi.getProduct().getProductImgUrl())
                        .quantity(oi.getQuantity())
                        .price(oi.getUnitPrice())
                        .lineTotal(oi.getLineTotal())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .customerId(order.getCustomer().getCustomerId())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .subtotal(order.getSubtotal())
                .deliveryFee(order.getDeliveryFee())
                .discount(order.getDiscount())
                .totalAmount(order.getTotalAmount())
                .deliveryAddress(order.getDeliveryAddress())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt())
                .items(itemResponses)

                .riderId(order.getRider() != null ? order.getRider().getRiderId() : null)
                .riderName(order.getRider() != null ? order.getRider().getName() : null)
                .build();
    }

    // ⭐ TOTAL SALES METHOD
    public BigDecimal getTotalSales() {
        return orderRepository.getTotalSales();
    }

    public long getTotalOrders() {
        return orderRepository.count();
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional
    public OrderResponse cancelOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Order already cancelled");
        }
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel delivered order");
        }

        order.setStatus(OrderStatus.CANCELLED);

        String baseNote = "Cancelled";
        if (reason != null && !reason.isBlank()) {
            baseNote += ": " + reason.trim();
        }
        if (order.getNotes() == null || order.getNotes().isBlank()) {
            order.setNotes(baseNote);
        } else {
            order.setNotes(order.getNotes() + " | " + baseNote);
        }

        // kung may updatedAt field ka:
        // order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

    @Transactional
    public OrderResponse markAsDelivered(Long orderId, String note) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot deliver cancelled order");
        }

        order.setStatus(OrderStatus.DELIVERED);

        String baseNote = "Delivered";
        if (note != null && !note.isBlank()) {
            baseNote += ": " + note.trim();
        }

        if (order.getNotes() == null || order.getNotes().isBlank()) {
            order.setNotes(baseNote);
        } else {
            order.setNotes(order.getNotes() + " | " + baseNote);
        }

        // kung may updatedAt field:
        // order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }
    public OrderResponse markInTransit(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot set in-transit after cancellation or delivery");
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);

    }
    public OrderResponse markProcessing(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot set processing after cancellation or delivery");
        }

        order.setStatus(OrderStatus.PROCESSING);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);
        return mapToResponse(saved);
    }

}
