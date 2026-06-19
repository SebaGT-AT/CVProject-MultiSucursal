package com.multisucursal.inventory.dashboard.dto;

import java.math.BigDecimal;

public record DashboardMetricResponse(
    long count,
    BigDecimal totalAmount
) {
}
