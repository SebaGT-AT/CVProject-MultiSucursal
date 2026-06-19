package com.multisucursal.inventory.sale.repository;

import com.multisucursal.inventory.sale.entity.Sale;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findBySaleNumberIgnoreCase(String saleNumber);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Sale> findAllByOrderBySaleDateDescIdDesc();

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    List<Sale> findByBranchIdOrderBySaleDateDescIdDesc(Long branchId);

    @EntityGraph(attributePaths = {"branch", "items", "items.product"})
    Optional<Sale> findById(Long id);
}
