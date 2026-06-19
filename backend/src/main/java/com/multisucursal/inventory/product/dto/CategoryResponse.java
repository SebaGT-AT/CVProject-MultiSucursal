package com.multisucursal.inventory.product.dto;

public record CategoryResponse(
    Long id,
    String name,
    String description,
    boolean active,
    int productCount
) {
}

