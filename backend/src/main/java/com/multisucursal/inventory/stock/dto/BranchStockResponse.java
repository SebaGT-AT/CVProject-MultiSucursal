package com.multisucursal.inventory.stock.dto;

public record BranchStockResponse(
    Long id,
    Long branchId,
    String branchCode,
    String branchName,
    Long productId,
    String productSku,
    String productName,
    Integer quantity,
    Integer minimumStock,
    boolean lowStock
) {
}

