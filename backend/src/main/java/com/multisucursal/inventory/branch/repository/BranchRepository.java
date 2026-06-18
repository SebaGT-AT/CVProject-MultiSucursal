package com.multisucursal.inventory.branch.repository;

import com.multisucursal.inventory.branch.entity.Branch;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    boolean existsByCodeIgnoreCase(String code);

    Optional<Branch> findByCodeIgnoreCase(String code);
}

