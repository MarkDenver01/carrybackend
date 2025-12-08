package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.cancelorder.CancelOrderRequest;
import com.carry_guide.carry_guide_admin.dto.request.cancelorder.DeliverOrderRequest;

import com.carry_guide.carry_guide_admin.service.MembershipService;
import com.carry_guide.carry_guide_admin.model.entity.SnowballPromo;
import com.carry_guide.carry_guide_admin.repository.JpaSnowballPromoRepository;
import com.carry_guide.carry_guide_admin.model.entity.Rider;
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
import com.carry_guide.carry_guide_admin.service.RiderService;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor

public class OrderService {

    private final JpaOrderRepository orderRepository;
    private final JpaProductRepository productRepository;
    private final JpaCustomerRepository customerRepository;
    private final RiderService riderService;
    private final MembershipService membershipService;
    private final JpaSnowballPromoRepository snowballPromoRepository;
    private final FcmNotificationService fcmSender;
    private final NotificationTokenService notificationTokenService;

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
                request.getDeliveryAddress() != null
                        ? request.getDeliveryAddress()
                        : customer.getAddress()
        );
        order.setNotes(request.getNotes());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        /* =============================
        ✅ BUILD ORDER + DEDUCT STOCK
        ============================== */
        for (OrderItemRequest itemReq : request.getItems()) {

            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + itemReq.getProductId()));

            BigDecimal unitPrice = getEffectivePrice(product);
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            subtotal = subtotal.add(lineTotal);

            int newStock = product.getStocks() - itemReq.getQuantity();

            if (newStock < 0) {
                throw new IllegalStateException("Insufficient stock for " + product.getProductName());
            }

            product.setStocks(newStock);
            if (newStock == 0) {
                product.setProductStatus("Out of Stock");
            }

            productRepository.save(product);

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

        BigDecimal deliveryFee = request.getDeliveryFee() != null
                ? request.getDeliveryFee()
                : BigDecimal.ZERO;

        BigDecimal discount = request.getDiscount() != null
                ? request.getDiscount()
                : BigDecimal.ZERO;

        BigDecimal total = subtotal.add(deliveryFee).subtract(discount);

        order.setDeliveryFee(deliveryFee);
        order.setDiscount(discount);
        order.setTotalAmount(total);

        /* =============================
        ✅ ✅ ✅ WALLET DEDUCTION (ONCE)
        ============================== */
        if (request.getPaymentMethod() == PaymentMethod.WALLET) {

            BigDecimal walletBalance = customer.getWalletBalance();

            if (walletBalance == null) {
                throw new IllegalStateException("Wallet not initialized");
            }

            if (walletBalance.compareTo(total) < 0) {
                throw new IllegalStateException("Insufficient wallet balance");
            }

            // ✅ DEDUCT WALLET HERE (TRANSACTION-SAFE)
            customer.setWalletBalance(walletBalance.subtract(total));
            customerRepository.save(customer);
        }

        Order saved = orderRepository.save(order);

        // for android notification
        notificationTokenService.getAndroidTokensForCustomer(customer.getCustomerId())
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Payment Successful 💳",
                        "Your payment for Order #" + saved.getId() + " was successful.",
                        Map.of(
                                "type", "PAYMENT_SUCCESS",
                                "orderId", saved.getId().toString()
                        )
                ));

        // for admin notification
        notificationTokenService.getAdminWebTokens()
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "New Paid Order ✅",
                        "Order #" + saved.getId() + " has been paid.",
                        Map.of(
                                "type", "ADMIN_PAYMENT_ALERT",
                                "orderId", saved.getId().toString()
                        )
                ));

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
                .customerName(
                        order.getCustomer().getUserName() != null
                                ? order.getCustomer().getUserName()
                                : order.getCustomer().getEmail()
                )
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

        // 1️⃣ RESTORE STOCKS FOR EACH ITEM
        for (OrderItem item : order.getItems()) {

            Product product = item.getProduct();

            int restored = product.getStocks() + item.getQuantity();
            product.setStocks(restored);

            //  ⭐ Auto rule: if stocks > 0 and previously Out of Stock → set to Available
            if (restored > 0 && "Out of Stock".equals(product.getProductStatus())) {
                product.setProductStatus("Available");
            }

            productRepository.save(product);
        }

        // 2️⃣ SET ORDER STATUS TO CANCELLED
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

        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        // notify web admin from customer
        notificationTokenService.getAdminWebTokens()
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Order Cancelled by Customer ⚠️",
                        "Order #" + saved.getId() + " was cancelled by the customer.",
                        Map.of(
                                "type", "CUSTOMER_CANCELLED",
                                "orderId", saved.getId().toString()
                        )
                ));

        // notify customer
        notificationTokenService.getAndroidTokensForCustomer(order.getCustomer().getCustomerId())
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Order Cancelled ❌",
                        "Your order #" + order.getId() + " was cancelled by the admin.",
                        Map.of(
                                "type", "ORDER_CANCELLED",
                                "orderId", order.getId().toString()
                        )
                ));

        return mapToResponse(saved);
    }


    public OrderResponse markAsDelivered(Long orderId, String note) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot deliver cancelled order");
        }

        order.setStatus(OrderStatus.DELIVERED);

        // ⭐ UPDATE RIDER STATUS TO AVAILABLE
        if (order.getRider() != null) {
            riderService.completeDelivery(order.getRider().getRiderId());
        }

        String baseNote = "Delivered";
        if (note != null && !note.isBlank()) {
            baseNote += ": " + note.trim();
        }

        if (order.getNotes() == null || order.getNotes().isBlank()) {
            order.setNotes(baseNote);
        } else {
            order.setNotes(order.getNotes() + " | " + baseNote);
        }

        order.setUpdatedAt(LocalDateTime.now());

        // ⭐⭐⭐ ADD POINTS HERE (BEST PLACE)
        try {
            int pointsToAdd = order.getTotalAmount()
                    .divide(new BigDecimal("10"))   // 1 point per ₱10
                    .intValue();

            if (pointsToAdd > 0) {
                membershipService.addPointsForCustomer(
                        order.getCustomer().getCustomerId(),
                        pointsToAdd
                );
            }

        } catch (Exception e) {
            System.out.println("⚠ Failed to add points: " + e.getMessage());
        }

        Order saved = orderRepository.save(order);

        // notify customer
        notificationTokenService.getAndroidTokensForCustomer(order.getCustomer().getCustomerId())
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Order Delivered 📦",
                        "Your order #" + order.getId() + " has been delivered.",
                        Map.of(
                                "type", "ORDER_DELIVERED",
                                "orderId", order.getId().toString()
                        )
                ));

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

        // NOTIFY CUSTOMER — ORDER ACCEPTED
        notificationTokenService.getAndroidTokensForCustomer(order.getCustomer().getCustomerId())
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Order Accepted ✅",
                        "Your order #" + order.getId() + " has been accepted by the store.",
                        Map.of(
                                "type", "ORDER_ACCEPTED",
                                "orderId", order.getId().toString()
                        )
                ));
        return mapToResponse(saved);
    }

    public OrderResponse markInTransit(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Cannot set In Transit after cancellation or delivery");
        }

        // 🔥 IMPORTANT: Cannot be In Transit if no assigned rider
        if (order.getRider() == null) {
            throw new IllegalStateException("Order cannot be set to In Transit without a rider.");
        }

        order.setStatus(OrderStatus.IN_TRANSIT);
        order.setUpdatedAt(LocalDateTime.now());

        Order saved = orderRepository.save(order);

        // notify customer
        notificationTokenService.getAndroidTokensForCustomer(order.getCustomer().getCustomerId())
                .forEach(t -> fcmSender.sendToToken(
                        t.getToken(),
                        "Rider Assigned 🚴",
                        "Your rider is on the way for Order #" + order.getId(),
                        Map.of(
                                "type", "RIDER_ASSIGNED",
                                "orderId", order.getId().toString()
                        )
                ));
        return mapToResponse(saved);
    }


    public OrderResponse assignRiderToOrder(Long orderId, Long riderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found"));

        Rider rider = riderService.getById(riderId);

        // set rider to order
        order.setRider(rider);
        orderRepository.save(order);

        // update rider status
        riderService.assignRider(riderId);
        return mapToResponse(order);
    }

    // ✅ CHECK IF PRODUCT HAS ACTIVE SNOWBALL PROMO
    // ✅ CHECK IF PRODUCT HAS ACTIVE SNOWBALL PROMO (WITH EXPIRY PROTECTION)
    private BigDecimal getEffectivePrice(Product product) {

        // get all snowball promos
        List<SnowballPromo> promos = snowballPromoRepository.findAll();

        for (SnowballPromo promo : promos) {

            // ✅ SKIP EXPIRED PROMOS
            if (promo.isHasExpiry() &&
                    promo.getExpiry() != null &&
                    promo.getExpiry().isBefore(java.time.LocalDate.now())) {
                continue; // laktawan ang expired promo
            }

            // ✅ check if product is part of this promo
            boolean isIncluded = promo.getProducts().stream()
                    .anyMatch(p -> p.getProductId().equals(product.getProductId()));

            if (isIncluded) {

                // ✅ SAFE FETCH PROMO PRICE
                Double promoPrice = promo.getPromoPrices() != null
                        ? promo.getPromoPrices().get(product.getProductId())
                        : null;

                if (promoPrice != null && promoPrice > 0) {
                    return BigDecimal.valueOf(promoPrice); // ✅ PROMO PRICE APPLIED
                }
            }
        }

        // ✅ FALLBACK TO NORMAL PRODUCT PRICE
        return product.getLatestPrice();
    }


}
