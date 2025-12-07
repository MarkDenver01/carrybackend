package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.CheckoutRequest;
import com.carry_guide.carry_guide_admin.dto.response.OrderResponse;
import com.carry_guide.carry_guide_admin.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.carry_guide.carry_guide_admin.dto.request.cancelorder.CancelOrderRequest;
import com.carry_guide.carry_guide_admin.dto.request.cancelorder.DeliverOrderRequest;


import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/public/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.checkout(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getCustomerOrders(
            @PathVariable Long customerId
    ) {
        return ResponseEntity.ok(orderService.getOrdersForCustomer(customerId));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrder(orderId));
    }

    @GetMapping("/total-sales")
    public ResponseEntity<BigDecimal> getTotalSales() {
        return ResponseEntity.ok(orderService.getTotalSales());
    }

    @GetMapping("/total-orders")
    public ResponseEntity<Long> getTotalOrders() {
        return ResponseEntity.ok(orderService.getTotalOrders());
    }

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody CancelOrderRequest request
    ) {
        return ResponseEntity.ok(
                orderService.cancelOrder(orderId, request.getReason())
        );
    }

    @PutMapping("/{orderId}/deliver")
    public ResponseEntity<OrderResponse> markAsDelivered(
            @PathVariable Long orderId,
            @RequestBody(required = false) DeliverOrderRequest request
    ) {
        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(
                orderService.markAsDelivered(orderId, note)
        );
    }

    @PutMapping("/{orderId}/in-transit")
    public ResponseEntity<OrderResponse> markInTransit(
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(orderService.markInTransit(orderId));

    }


    @PutMapping("/{orderId}/processing")
    public ResponseEntity<OrderResponse> markProcessing(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                orderService.markProcessing(orderId)
        );
    }

    @PutMapping("/{orderId}/assign-rider/{riderId}")
    public ResponseEntity<OrderResponse> assignRiderToOrder(
            @PathVariable Long orderId,
            @PathVariable Long riderId
    ) {
        return ResponseEntity.ok(orderService.assignRiderToOrder(orderId, riderId));
    }


}
