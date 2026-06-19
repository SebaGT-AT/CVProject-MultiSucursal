package com.multisucursal.inventory.purchase.entity;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "purchases")
public class Purchase extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String purchaseNumber;

    @Column(nullable = false, length = 120)
    private String supplierName;

    @Column(nullable = false)
    private LocalDate purchaseDate;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount;

    @Column(length = 255)
    private String notes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PurchaseItem> items = new LinkedHashSet<>();
}
