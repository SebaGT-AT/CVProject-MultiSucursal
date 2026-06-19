package com.multisucursal.inventory.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ProductRequest(
    @NotBlank
    @Size(max = 50)
    String sku,
    @NotBlank
    @Size(max = 150)
    String name,
    @Size(max = 500)
    String description,
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal purchasePrice,
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    BigDecimal salePrice,
    boolean active,
    @NotNull
    Long categoryId
) {
}

