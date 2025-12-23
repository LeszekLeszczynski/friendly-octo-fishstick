package com.decerto.leszek.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticsServiceTest {

    private AnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsService();
    }

    @Test
    void shouldCalculateStatsForSingleCompletedOrder() {
        var electronics = new Product("Laptop", "Electronics", new BigDecimal("999.99"));
        var clothing = new Product("T-Shirt", "Clothing", new BigDecimal("19.99"));

        var order = new Order("ORD-001", OrderStatus.COMPLETED, List.of(electronics, clothing));

        var result = service.analyzeSales(List.of(order));

        assertEquals(2, result.size());
        assertEquals(new CategoryStats(1, new BigDecimal("999.99")), result.get("Electronics"));
        assertEquals(new CategoryStats(1, new BigDecimal("19.99")), result.get("Clothing"));
    }

    @Test
    void shouldFilterOutCancelledOrders() {
        var product1 = new Product("Phone", "Electronics", new BigDecimal("599.99"));
        var product2 = new Product("Tablet", "Electronics", new BigDecimal("399.99"));

        var completedOrder = new Order("ORD-001", OrderStatus.COMPLETED, List.of(product1));
        var cancelledOrder = new Order("ORD-002", OrderStatus.CANCELLED, List.of(product2));

        var result = service.analyzeSales(List.of(completedOrder, cancelledOrder));

        assertEquals(1, result.size());
        var stats = result.get("Electronics");
        assertEquals(1, stats.quantitySold());
        assertEquals(new BigDecimal("599.99"), stats.totalRevenue());
    }

    @Test
    void shouldAggregateMultipleProductsInSameCategory() {
        var phone = new Product("Phone", "Electronics", new BigDecimal("599.99"));
        var laptop = new Product("Laptop", "Electronics", new BigDecimal("999.99"));
        var tablet = new Product("Tablet", "Electronics", new BigDecimal("399.99"));

        var order1 = new Order("ORD-001", OrderStatus.COMPLETED, List.of(phone, laptop));
        var order2 = new Order("ORD-002", OrderStatus.COMPLETED, List.of(tablet));

        var result = service.analyzeSales(List.of(order1, order2));

        assertEquals(1, result.size());
        var stats = result.get("Electronics");
        assertEquals(3, stats.quantitySold());
        assertEquals(new BigDecimal("1999.97"), stats.totalRevenue());
    }

    @Test
    void shouldHandleMultipleCategoriesAcrossMultipleOrders() {
        var laptop = new Product("Laptop", "Electronics", new BigDecimal("1200.00"));
        var phone = new Product("Phone", "Electronics", new BigDecimal("800.00"));
        var shirt = new Product("T-Shirt", "Clothing", new BigDecimal("25.00"));
        var jeans = new Product("Jeans", "Clothing", new BigDecimal("60.00"));
        var novel = new Product("Novel", "Books", new BigDecimal("15.99"));

        var order1 = new Order("ORD-001", OrderStatus.COMPLETED, List.of(laptop, shirt));
        var order2 = new Order("ORD-002", OrderStatus.PENDING, List.of(phone, jeans, novel));
        var order3 = new Order("ORD-003", OrderStatus.CANCELLED, List.of(
                new Product("Headphones", "Electronics", new BigDecimal("150.00"))
        ));

        var result = service.analyzeSales(List.of(order1, order2, order3));

        assertEquals(3, result.size());

        var electronicsStats = result.get("Electronics");
        assertEquals(2, electronicsStats.quantitySold());
        assertEquals(new BigDecimal("2000.00"), electronicsStats.totalRevenue());

        var clothingStats = result.get("Clothing");
        assertEquals(2, clothingStats.quantitySold());
        assertEquals(new BigDecimal("85.00"), clothingStats.totalRevenue());

        var booksStats = result.get("Books");
        assertEquals(1, booksStats.quantitySold());
        assertEquals(new BigDecimal("15.99"), booksStats.totalRevenue());
    }

    @Test
    void shouldReturnEmptyMapForEmptyOrdersList() {
        var result = service.analyzeSales(List.of());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnEmptyMapWhenAllOrdersAreCancelled() {
        var product = new Product("Product", "Category", new BigDecimal("100.00"));
        var cancelledOrder1 = new Order("ORD-001", OrderStatus.CANCELLED, List.of(product));
        var cancelledOrder2 = new Order("ORD-002", OrderStatus.CANCELLED, List.of(product));

        var result = service.analyzeSales(List.of(cancelledOrder1, cancelledOrder2));

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldHandleOrdersWithEmptyProductList() {
        var product = new Product("Laptop", "Electronics", new BigDecimal("999.99"));
        var orderWithProducts = new Order("ORD-001", OrderStatus.COMPLETED, List.of(product));
        var orderWithoutProducts = new Order("ORD-002", OrderStatus.COMPLETED, List.of());

        var result = service.analyzeSales(List.of(orderWithProducts, orderWithoutProducts));

        assertEquals(1, result.size());
        var stats = result.get("Electronics");
        assertEquals(1, stats.quantitySold());
        assertEquals(new BigDecimal("999.99"), stats.totalRevenue());
    }

    @Test
    void shouldHandlePendingOrdersCorrectly() {
        var product = new Product("Mouse", "Electronics", new BigDecimal("29.99"));
        var pendingOrder = new Order("ORD-001", OrderStatus.PENDING, List.of(product));

        var result = service.analyzeSales(List.of(pendingOrder));

        assertEquals(1, result.size());
        var stats = result.get("Electronics");
        assertEquals(1, stats.quantitySold());
        assertEquals(new BigDecimal("29.99"), stats.totalRevenue());
    }

    @Test
    void shouldCalculateCorrectRevenueWithDecimalPrices() {
        var item1 = new Product("Item1", "Test", new BigDecimal("10.50"));
        var item2 = new Product("Item2", "Test", new BigDecimal("20.75"));
        var item3 = new Product("Item3", "Test", new BigDecimal("5.25"));

        var order = new Order("ORD-001", OrderStatus.COMPLETED, List.of(item1, item2, item3));

        var result = service.analyzeSales(List.of(order));

        var stats = result.get("Test");
        assertEquals(3, stats.quantitySold());
        assertEquals(new BigDecimal("36.50"), stats.totalRevenue());
    }

    @Test
    void shouldHandleLargeNumberOfOrders() {
        var orders = List.of(
                new Order("ORD-001", OrderStatus.COMPLETED, List.of(
                        new Product("P1", "Cat1", new BigDecimal("100")),
                        new Product("P2", "Cat2", new BigDecimal("200"))
                )),
                new Order("ORD-002", OrderStatus.PENDING, List.of(
                        new Product("P3", "Cat1", new BigDecimal("150")),
                        new Product("P4", "Cat3", new BigDecimal("300"))
                )),
                new Order("ORD-003", OrderStatus.CANCELLED, List.of(
                        new Product("P5", "Cat1", new BigDecimal("999"))
                )),
                new Order("ORD-004", OrderStatus.COMPLETED, List.of(
                        new Product("P6", "Cat2", new BigDecimal("250")),
                        new Product("P7", "Cat1", new BigDecimal("175"))
                ))
        );

        var result = service.analyzeSales(orders);

        assertEquals(3, result.size());

        var cat1Stats = result.get("Cat1");
        assertEquals(3, cat1Stats.quantitySold());
        assertEquals(new BigDecimal("425"), cat1Stats.totalRevenue());

        var cat2Stats = result.get("Cat2");
        assertEquals(2, cat2Stats.quantitySold());
        assertEquals(new BigDecimal("450"), cat2Stats.totalRevenue());

        var cat3Stats = result.get("Cat3");
        assertEquals(1, cat3Stats.quantitySold());
        assertEquals(new BigDecimal("300"), cat3Stats.totalRevenue());
    }
}