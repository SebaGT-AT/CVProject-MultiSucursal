package com.multisucursal.inventory.stock.service;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.branch.repository.BranchRepository;
import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.product.entity.Product;
import com.multisucursal.inventory.product.repository.ProductRepository;
import com.multisucursal.inventory.stock.dto.AdjustmentType;
import com.multisucursal.inventory.stock.dto.BranchStockResponse;
import com.multisucursal.inventory.stock.dto.StockAdjustmentRequest;
import com.multisucursal.inventory.stock.dto.StockMovementResponse;
import com.multisucursal.inventory.stock.dto.StockTransferRequest;
import com.multisucursal.inventory.stock.entity.BranchStock;
import com.multisucursal.inventory.stock.entity.MovementType;
import com.multisucursal.inventory.stock.entity.StockMovement;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import com.multisucursal.inventory.stock.repository.StockMovementRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StockService {

    private final BranchStockRepository branchStockRepository;
    private final StockMovementRepository stockMovementRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<BranchStockResponse> findAllStocks() {
        return branchStockRepository.findAllByOrderByBranchNameAscProductNameAsc()
            .stream()
            .map(this::toStockResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BranchStockResponse> findStocksByBranch(Long branchId) {
        ensureBranchExists(branchId);
        return branchStockRepository.findByBranchIdOrderByProductNameAsc(branchId)
            .stream()
            .map(this::toStockResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<BranchStockResponse> findStocksByProduct(Long productId) {
        ensureProductExists(productId);
        return branchStockRepository.findByProductIdOrderByBranchNameAsc(productId)
            .stream()
            .map(this::toStockResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findAllMovements() {
        return stockMovementRepository.findAllByOrderByOccurredAtDesc()
            .stream()
            .map(this::toMovementResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findMovementsByBranch(Long branchId) {
        ensureBranchExists(branchId);
        return stockMovementRepository.findByBranchIdOrderByOccurredAtDesc(branchId)
            .stream()
            .map(this::toMovementResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<StockMovementResponse> findMovementsByProduct(Long productId) {
        ensureProductExists(productId);
        return stockMovementRepository.findByProductIdOrderByOccurredAtDesc(productId)
            .stream()
            .map(this::toMovementResponse)
            .toList();
    }

    @Transactional
    public BranchStockResponse adjustStock(StockAdjustmentRequest request) {
        Branch branch = getBranchById(request.branchId());
        Product product = getProductById(request.productId());
        BranchStock branchStock = getOrCreateBranchStock(branch, product);

        if (request.minimumStock() != null) {
            branchStock.setMinimumStock(request.minimumStock());
        }

        int currentQuantity = branchStock.getQuantity();
        int updatedQuantity;
        MovementType movementType;

        switch (request.adjustmentType()) {
            case INITIAL_LOAD -> {
                if (currentQuantity > 0) {
                    throw new IllegalStateException("Initial load is only allowed when stock is zero");
                }
                updatedQuantity = request.quantity();
                movementType = MovementType.INITIAL_LOAD;
            }
            case INCREASE -> {
                updatedQuantity = currentQuantity + request.quantity();
                movementType = MovementType.ADJUSTMENT;
            }
            case DECREASE -> {
                updatedQuantity = currentQuantity - request.quantity();
                if (updatedQuantity < 0) {
                    throw new IllegalStateException("Insufficient stock for decrease adjustment");
                }
                movementType = MovementType.ADJUSTMENT;
            }
            default -> throw new IllegalStateException("Unsupported adjustment type");
        }

        branchStock.setQuantity(updatedQuantity);
        BranchStock savedStock = branchStockRepository.save(branchStock);

        String notes = request.notes();
        if (request.adjustmentType() == AdjustmentType.INCREASE) {
            notes = appendPrefix("Increase adjustment", notes);
        } else if (request.adjustmentType() == AdjustmentType.DECREASE) {
            notes = appendPrefix("Decrease adjustment", notes);
        }

        createMovement(branch, product, movementType, request.quantity(), request.reference(), notes);
        return toStockResponse(savedStock);
    }

    @Transactional
    public void transferStock(StockTransferRequest request) {
        if (request.sourceBranchId().equals(request.targetBranchId())) {
            throw new IllegalArgumentException("Source and target branches must be different");
        }

        Branch sourceBranch = getBranchById(request.sourceBranchId());
        Branch targetBranch = getBranchById(request.targetBranchId());
        Product product = getProductById(request.productId());

        BranchStock sourceStock = branchStockRepository.findByBranchIdAndProductId(sourceBranch.getId(), product.getId())
            .orElseThrow(() -> new IllegalStateException("Source branch has no stock for the selected product"));

        if (sourceStock.getQuantity() < request.quantity()) {
            throw new IllegalStateException("Insufficient stock for transfer");
        }

        BranchStock targetStock = getOrCreateBranchStock(targetBranch, product);

        sourceStock.setQuantity(sourceStock.getQuantity() - request.quantity());
        targetStock.setQuantity(targetStock.getQuantity() + request.quantity());

        branchStockRepository.save(sourceStock);
        branchStockRepository.save(targetStock);

        createMovement(
            sourceBranch,
            product,
            MovementType.TRANSFER_OUT,
            request.quantity(),
            request.reference(),
            appendPrefix("Transfer to " + targetBranch.getCode(), request.notes())
        );

        createMovement(
            targetBranch,
            product,
            MovementType.TRANSFER_IN,
            request.quantity(),
            request.reference(),
            appendPrefix("Transfer from " + sourceBranch.getCode(), request.notes())
        );
    }

    private BranchStock getOrCreateBranchStock(Branch branch, Product product) {
        return branchStockRepository.findByBranchIdAndProductId(branch.getId(), product.getId())
            .orElseGet(() -> {
                BranchStock branchStock = new BranchStock();
                branchStock.setBranch(branch);
                branchStock.setProduct(product);
                branchStock.setQuantity(0);
                branchStock.setMinimumStock(0);
                return branchStock;
            });
    }

    private void createMovement(
        Branch branch,
        Product product,
        MovementType movementType,
        Integer quantity,
        String reference,
        String notes
    ) {
        StockMovement movement = new StockMovement();
        movement.setBranch(branch);
        movement.setProduct(product);
        movement.setMovementType(movementType);
        movement.setQuantity(quantity);
        movement.setReference(reference);
        movement.setNotes(notes);
        movement.setOccurredAt(LocalDateTime.now());
        stockMovementRepository.save(movement);
    }

    private Branch getBranchById(Long branchId) {
        return branchRepository.findById(branchId)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
    }

    private void ensureBranchExists(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with id " + branchId);
        }
    }

    private void ensureProductExists(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id " + productId);
        }
    }

    private String appendPrefix(String prefix, String notes) {
        if (notes == null || notes.isBlank()) {
            return prefix;
        }
        return prefix + " - " + notes.trim();
    }

    private BranchStockResponse toStockResponse(BranchStock branchStock) {
        return new BranchStockResponse(
            branchStock.getId(),
            branchStock.getBranch().getId(),
            branchStock.getBranch().getCode(),
            branchStock.getBranch().getName(),
            branchStock.getProduct().getId(),
            branchStock.getProduct().getSku(),
            branchStock.getProduct().getName(),
            branchStock.getQuantity(),
            branchStock.getMinimumStock(),
            branchStock.getQuantity() <= branchStock.getMinimumStock()
        );
    }

    private StockMovementResponse toMovementResponse(StockMovement movement) {
        return new StockMovementResponse(
            movement.getId(),
            movement.getBranch().getId(),
            movement.getBranch().getCode(),
            movement.getBranch().getName(),
            movement.getProduct().getId(),
            movement.getProduct().getSku(),
            movement.getProduct().getName(),
            movement.getMovementType(),
            movement.getQuantity(),
            movement.getReference(),
            movement.getNotes(),
            movement.getOccurredAt()
        );
    }
}

