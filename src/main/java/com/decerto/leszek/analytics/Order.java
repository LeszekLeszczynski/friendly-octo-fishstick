package com.decerto.leszek.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
class Order {
    private String id;
    private OrderStatus status;
    private List<Product> products;
}