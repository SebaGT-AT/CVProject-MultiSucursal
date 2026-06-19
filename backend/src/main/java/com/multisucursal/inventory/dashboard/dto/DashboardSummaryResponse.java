package com.multisucursal.inventory.dashboard.dto;

import java.time.LocalDate;
import java.util.List;

public record DashboardSummaryResponse(
    LocalDate generatedDate,
    DashboardMetricResponse salesToday,
    DashboardMetricResponse purchasesThisMonth,
    List<DashboardLowStockResponse> lowStockProducts
) {
}
