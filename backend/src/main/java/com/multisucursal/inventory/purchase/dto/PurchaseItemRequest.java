package com.multisucursal.inventory.purchase.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PurchaseItemRequest(
    @NotNull(message = "Product id is required")
    Long productId,

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be greater than zero")
    Integer quantity,

    @NotNull(message = "Unit purchase price is required")
    @DecimalMin(value = "0.01", message = "Unit purchase price must be greater than zero")
    BigDecimal unitPurchasePrice
) {
}
