package com.decerto.leszek.analytics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class AnalyticsService {

    /**
     * Filter out orders with status "CANCELLED".
     * For each product category, calculate:
     * Count: Total number of items sold.
     * Revenue: Total value of items sold (Sum of prices).
     */
    public Map<String, CategoryStats> analyzeSales(List<Order> orders) {
        return Map.of();
    }
}