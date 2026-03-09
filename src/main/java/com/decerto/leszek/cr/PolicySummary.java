package com.decerto.leszek.cr;

import lombok.Data;

import java.math.BigDecimal;

@Data
class PolicySummary {

    private Long id;
    private String number;
    private BigDecimal premium;
    private String clientName;
}
