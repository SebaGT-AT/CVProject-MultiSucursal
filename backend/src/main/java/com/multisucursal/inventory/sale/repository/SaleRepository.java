package com.multisucursal.inventory.sale.repository;

import com.multisucursal.inventory.sale.entity.Sale;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findBySaleNumberIgnoreCase(String saleNumber);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Sale> findAllByOrderBySaleDateDescIdDesc();

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Sale> findByBranchIdOrderBySaleDateDescIdDesc(Long branchId);

    long countBySaleDate(LocalDate saleDate);

    @Query("select coalesce(sum(s.totalAmount), 0) from Sale s where s.saleDate = :saleDate")
    Optional<BigDecimal> sumTotalAmountBySaleDate(LocalDate saleDate);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<Sale> findById(Long id);
}
