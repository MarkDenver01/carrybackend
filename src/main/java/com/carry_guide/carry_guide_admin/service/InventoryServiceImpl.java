package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.analytics.InventoryMetricsResponseDTO;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import com.carry_guide.carry_guide_admin.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final JpaProductRepository productRepository;

    @Override
    public InventoryMetricsResponseDTO getMetrics() {

        int lowStockThreshold = 10;

        long lowStock = productRepository.countLowStock(lowStockThreshold);
        long outOfStock = productRepository.countOutOfStock();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime limit = now.plusDays(10);

        long expiringSoon = productRepository.countExpiringSoon(now, limit);

        return new InventoryMetricsResponseDTO(lowStock, outOfStock, expiringSoon);
    }
}