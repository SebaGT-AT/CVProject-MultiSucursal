package com.multisucursal.inventory.stock.entity;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.common.entity.BaseEntity;
import com.multisucursal.inventory.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    name = "branch_stocks",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_branch_stock_branch_product", columnNames = {"branch_id", "product_id"})
    }
)
public class BranchStock extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity = 0;

    @Column(nullable = false)
    private Integer minimumStock = 0;
}

