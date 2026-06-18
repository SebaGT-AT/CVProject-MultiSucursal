package com.multisucursal.inventory.stock.repository;

import com.multisucursal.inventory.stock.entity.BranchStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchStockRepository extends JpaRepository<BranchStock, Long> {

    Optional<BranchStock> findByBranchIdAndProductId(Long branchId, Long productId);

    List<BranchStock> findByBranchId(Long branchId);

    List<BranchStock> findByProductId(Long productId);
}

