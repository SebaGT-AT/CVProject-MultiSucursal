package com.multisucursal.inventory.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record PurchaseRequest(
    @NotBlank(message = "Purchase number is required")
    String purchaseNumber,

    @NotBlank(message = "Supplier name is required")
    String supplierName,

    @NotNull(message = "Purchase date is required")
    LocalDate purchaseDate,

    @NotNull(message = "Branch id is required")
    Long branchId,

    String notes,

    @NotEmpty(message = "At least one purchase item is required")
    List<@Valid PurchaseItemRequest> items
) {
}
