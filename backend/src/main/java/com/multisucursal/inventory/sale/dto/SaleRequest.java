package com.multisucursal.inventory.sale.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record SaleRequest(
    @NotBlank(message = "Sale number is required")
    String saleNumber,

    @NotBlank(message = "Customer name is required")
    String customerName,

    @NotNull(message = "Sale date is required")
    LocalDate saleDate,

    @NotNull(message = "Branch id is required")
    Long branchId,

    String notes,

    @NotEmpty(message = "At least one sale item is required")
    List<@Valid SaleItemRequest> items
) {
}
