package com.multisucursal.inventory.stock.repository;

import com.multisucursal.inventory.stock.entity.StockMovement;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    List<StockMovement> findByBranchIdOrderByOccurredAtDesc(Long branchId);

    List<StockMovement> findByProductIdOrderByOccurredAtDesc(Long productId);
}

