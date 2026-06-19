package com.multisucursal.inventory.purchase.dto;

import java.math.BigDecimal;

public record PurchaseItemResponse(
    Long id,
    Long productId,
    String productSku,
    String productName,
    Integer quantity,
    BigDecimal unitPurchasePrice,
    BigDecimal subtotal
) {
}
