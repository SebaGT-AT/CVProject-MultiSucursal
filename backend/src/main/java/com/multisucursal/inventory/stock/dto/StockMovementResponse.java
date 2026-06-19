package com.multisucursal.inventory.stock.dto;

import com.multisucursal.inventory.stock.entity.MovementType;
import java.time.LocalDateTime;

public record StockMovementResponse(
    Long id,
    Long branchId,
    String branchCode,
    String branchName,
    Long productId,
    String productSku,
    String productName,
    MovementType movementType,
    Integer quantity,
    String reference,
    String notes,
    LocalDateTime occurredAt
) {
}

