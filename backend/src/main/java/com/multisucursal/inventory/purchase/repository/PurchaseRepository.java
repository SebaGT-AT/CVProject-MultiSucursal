package com.multisucursal.inventory.purchase.repository;

import com.multisucursal.inventory.purchase.entity.Purchase;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    Optional<Purchase> findByPurchaseNumberIgnoreCase(String purchaseNumber);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Purchase> findAllByOrderByPurchaseDateDescIdDesc();

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Purchase> findByBranchIdOrderByPurchaseDateDescIdDesc(Long branchId);

    long countByPurchaseDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("select coalesce(sum(p.totalAmount), 0) from Purchase p where p.purchaseDate between :startDate and :endDate")
    Optional<BigDecimal> sumTotalAmountByPurchaseDateBetween(LocalDate startDate, LocalDate endDate);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<Purchase> findById(Long id);
}
