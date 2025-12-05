package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.response.analytics.SalesAnalyticsResponse;
import com.carry_guide.carry_guide_admin.repository.JpaSalesAnalyticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SalesAnalyticsService {

    private final JpaSalesAnalyticsRepository repo;

    public SalesAnalyticsResponse getSalesReport(String rangeId) {

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (rangeId) {
            case "7d" -> start = end.minusDays(7);
            case "6m" -> start = end.minusMonths(6);
            case "3y" -> start = end.minusYears(3);
            default -> start = end.minusDays(7);
        }

        var categorySales = repo.getCategorySales(start, end);
        var productSales = repo.getProductSales(start, end);

        return new SalesAnalyticsResponse(
                rangeId,
                categorySales,
                productSales
        );
    }
}