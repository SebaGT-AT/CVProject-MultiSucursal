package com.multisucursal.inventory.purchase.repository;

import com.multisucursal.inventory.purchase.entity.Purchase;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByPurchaseNumberIgnoreCase(String purchaseNumber);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Purchase> findAllByOrderByPurchaseDateDescIdDesc();

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Purchase> findByBranchIdOrderByPurchaseDateDescIdDesc(Long branchId);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<Purchase> findById(Long id);
}
