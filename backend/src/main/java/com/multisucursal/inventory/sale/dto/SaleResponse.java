package com.multisucursal.inventory.sale.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SaleResponse(
    Long id,
    String saleNumber,
    String customerName,
    LocalDate saleDate,
    Long branchId,
    String branchCode,
    String branchName,
    BigDecimal totalAmount,
    String notes,
    List<SaleItemResponse> items
) {
}
