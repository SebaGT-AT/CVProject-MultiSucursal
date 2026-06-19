package com.multisucursal.inventory.sale.dto;

import java.math.BigDecimal;

public record SaleItemResponse(
    Long id,
    Long productId,
    String productSku,
    String productName,
    Integer quantity,
    BigDecimal unitSalePrice,
    BigDecimal subtotal
) {
}
