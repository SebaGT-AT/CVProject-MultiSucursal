package com.multisucursal.inventory.branch.service;

import com.multisucursal.inventory.branch.dto.BranchRequest;
import com.multisucursal.inventory.branch.dto.BranchResponse;
import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.branch.repository.BranchRepository;
import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import com.multisucursal.inventory.stock.repository.StockMovementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<BranchResponse> findAll() {
        return branchRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public BranchResponse findById(Long id) {
        return toResponse(getBranchById(id));
    }

    @Transactional
    public BranchResponse create(BranchRequest request) {
        validateUniqueCode(request.code(), null);
        validateUniqueEmail(request.email(), null);

        Branch branch = new Branch();
        applyRequest(branch, request);
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public BranchResponse update(Long id, BranchRequest request) {
        Branch branch = getBranchById(id);
        validateUniqueCode(request.code(), id);
        validateUniqueEmail(request.email(), id);

        applyRequest(branch, request);
        return toResponse(branchRepository.save(branch));
    }

    @Transactional
    public void delete(Long id) {
        Branch branch = getBranchById(id);
        if (branchStockRepository.existsByBranchId(id) || stockMovementRepository.existsByBranchId(id)) {
            throw new IllegalStateException("Cannot delete branch with stock history");
        }
        branchRepository.delete(branch);
    }

    private Branch getBranchById(Long id) {
        return branchRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + id));
    }

    private void validateUniqueCode(String code, Long currentId) {
        branchRepository.findByCodeIgnoreCase(code.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Branch code is already in use");
            });
    }

    private void validateUniqueEmail(String email, Long currentId) {
        if (!StringUtils.hasText(email)) {
            return;
        }

        branchRepository.findByEmailIgnoreCase(email.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Branch email is already in use");
            });
    }

    private void applyRequest(Branch branch, BranchRequest request) {
        branch.setCode(request.code().trim().toUpperCase());
        branch.setName(request.name().trim());
        branch.setCity(request.city().trim());
        branch.setAddress(request.address().trim());
        branch.setManagerName(trimToNull(request.managerName()));
        branch.setPhone(trimToNull(request.phone()));
        branch.setEmail(normalizeEmail(request.email()));
        branch.setActive(request.active());
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        return email.trim().toLowerCase();
    }

    private BranchResponse toResponse(Branch branch) {
        return new BranchResponse(
            branch.getId(),
            branch.getCode(),
            branch.getName(),
            branch.getCity(),
            branch.getAddress(),
            branch.getManagerName(),
            branch.getPhone(),
            branch.getEmail(),
            branch.isActive()
        );
    }
}

