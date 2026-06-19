package com.multisucursal.inventory.dashboard.dto;

public record DashboardLowStockResponse(
    Long branchId,
    String branchCode,
    String branchName,
    Long productId,
    String productSku,
    String productName,
    Integer quantity,
    Integer minimumStock
) {
}
