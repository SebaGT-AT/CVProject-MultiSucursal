package com.multisucursal.inventory.stock.repository;

import com.multisucursal.inventory.stock.entity.BranchStock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BranchStockRepository extends JpaRepository<BranchStock, Long> {

    Optional<BranchStock> findByBranchIdAndProductId(Long branchId, Long productId);

    List<BranchStock> findByBranchId(Long branchId);

    List<BranchStock> findByProductId(Long productId);

    List<BranchStock> findAllByOrderByBranchNameAscProductNameAsc();

    List<BranchStock> findByBranchIdOrderByProductNameAsc(Long branchId);

    List<BranchStock> findByProductIdOrderByBranchNameAsc(Long productId);

    @Query("""
        select bs
        from BranchStock bs
        where bs.quantity <= bs.minimumStock
        order by bs.branch.name, bs.product.name
        """)
    List<BranchStock> findAllByQuantityLessThanEqualMinimumStock();

    boolean existsByBranchId(Long branchId);

    boolean existsByProductId(Long productId);
}
