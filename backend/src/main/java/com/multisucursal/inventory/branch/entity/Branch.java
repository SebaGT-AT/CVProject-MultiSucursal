package com.multisucursal.inventory.branch.entity;

import com.multisucursal.inventory.common.entity.BaseEntity;
import com.multisucursal.inventory.stock.entity.BranchStock;
import com.multisucursal.inventory.stock.entity.StockMovement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "branches")
public class Branch extends BaseEntity {

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 150)
    private String city;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 80)
    private String managerName;

    @Column(length = 30)
    private String phone;

    @Column(length = 120)
    private String email;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<BranchStock> branchStocks = new LinkedHashSet<>();

    @OneToMany(mappedBy = "branch", fetch = FetchType.LAZY)
    private Set<StockMovement> stockMovements = new LinkedHashSet<>();
}

