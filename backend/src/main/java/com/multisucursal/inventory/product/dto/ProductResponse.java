package com.multisucursal.inventory.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
    Long id,
    String sku,
    String name,
    String description,
    BigDecimal purchasePrice,
    BigDecimal salePrice,
    boolean active,
    Long categoryId,
    String categoryName
) {
}

