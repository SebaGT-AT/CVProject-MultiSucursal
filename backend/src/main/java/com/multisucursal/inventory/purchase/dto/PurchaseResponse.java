package com.multisucursal.inventory.purchase.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PurchaseResponse(
    Long id,
    String purchaseNumber,
    String supplierName,
    LocalDate purchaseDate,
    Long branchId,
    String branchCode,
    String branchName,
    BigDecimal totalAmount,
    String notes,
    List<PurchaseItemResponse> items
) {
}
