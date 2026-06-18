package com.multisucursal.inventory.stock.entity;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.common.entity.BaseEntity;
import com.multisucursal.inventory.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "stock_movements")
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 120)
    private String reference;

    @Column(length = 255)
    private String notes;

    @Column(nullable = false)
    private LocalDateTime occurredAt;
}

