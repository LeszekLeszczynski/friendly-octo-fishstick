package com.decerto.leszek.analytics;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
class Product {
    private String name;
    private String category;
    private BigDecimal price;
}