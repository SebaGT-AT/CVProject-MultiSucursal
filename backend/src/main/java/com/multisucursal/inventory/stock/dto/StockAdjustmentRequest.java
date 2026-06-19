package com.multisucursal.inventory.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StockAdjustmentRequest(
    @NotNull
    Long branchId,
    @NotNull
    Long productId,
    @NotNull
    @Min(1)
    Integer quantity,
    @NotNull
    AdjustmentType adjustmentType,
    @Min(0)
    Integer minimumStock,
    @NotBlank
    @Size(max = 120)
    String reference,
    @Size(max = 255)
    String notes
) {
}

