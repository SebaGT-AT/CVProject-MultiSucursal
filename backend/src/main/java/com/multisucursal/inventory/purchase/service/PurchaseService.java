package com.multisucursal.inventory.purchase.service;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.branch.repository.BranchRepository;
import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.product.entity.Product;
import com.multisucursal.inventory.product.repository.ProductRepository;
import com.multisucursal.inventory.purchase.dto.PurchaseItemRequest;
import com.multisucursal.inventory.purchase.dto.PurchaseItemResponse;
import com.multisucursal.inventory.purchase.dto.PurchaseRequest;
import com.multisucursal.inventory.purchase.dto.PurchaseResponse;
import com.multisucursal.inventory.purchase.entity.Purchase;
import com.multisucursal.inventory.purchase.entity.PurchaseItem;
import com.multisucursal.inventory.purchase.repository.PurchaseRepository;
import com.multisucursal.inventory.stock.entity.BranchStock;
import com.multisucursal.inventory.stock.entity.MovementType;
import com.multisucursal.inventory.stock.entity.StockMovement;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import com.multisucursal.inventory.stock.repository.StockMovementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<PurchaseResponse> findAll() {
        return purchaseRepository.findAllByOrderByPurchaseDateDescIdDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> findByBranch(Long branchId) {
        ensureBranchExists(branchId);
        return purchaseRepository.findByBranchIdOrderByPurchaseDateDescIdDesc(branchId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public PurchaseResponse findById(Long id) {
        return toResponse(getPurchaseById(id));
    }

    @Transactional
    public PurchaseResponse create(PurchaseRequest request) {
        validateUniquePurchaseNumber(request.purchaseNumber(), null);
        Branch branch = getBranchById(request.branchId());

        Purchase purchase = new Purchase();
        purchase.setPurchaseNumber(request.purchaseNumber().trim().toUpperCase());
        purchase.setSupplierName(request.supplierName().trim());
        purchase.setPurchaseDate(request.purchaseDate());
        purchase.setBranch(branch);
        purchase.setNotes(trimToNull(request.notes()));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (PurchaseItemRequest itemRequest : request.items()) {
            Product product = getProductById(itemRequest.productId());
            PurchaseItem item = buildPurchaseItem(purchase, product, itemRequest);
            purchase.getItems().add(item);
            totalAmount = totalAmount.add(item.getSubtotal());

            updateBranchStock(branch, product, itemRequest.quantity());
            createMovement(branch, product, itemRequest.quantity(), purchase.getPurchaseNumber(), purchase.getSupplierName());
        }

        purchase.setTotalAmount(totalAmount);
        return toResponse(purchaseRepository.save(purchase));
    }

    private Purchase getPurchaseById(Long id) {
        return purchaseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Purchase not found with id " + id));
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

    private void validateUniquePurchaseNumber(String purchaseNumber, Long currentId) {
        purchaseRepository.findByPurchaseNumberIgnoreCase(purchaseNumber.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Purchase number is already in use");
            });
    }

    private PurchaseItem buildPurchaseItem(Purchase purchase, Product product, PurchaseItemRequest request) {
        PurchaseItem item = new PurchaseItem();
        item.setPurchase(purchase);
        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setUnitPurchasePrice(request.unitPurchasePrice());
        item.setSubtotal(request.unitPurchasePrice().multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private void updateBranchStock(Branch branch, Product product, Integer quantity) {
        BranchStock branchStock = branchStockRepository.findByBranchIdAndProductId(branch.getId(), product.getId())
            .orElseGet(() -> {
                BranchStock stock = new BranchStock();
                stock.setBranch(branch);
                stock.setProduct(product);
                stock.setQuantity(0);
                stock.setMinimumStock(0);
                return stock;
            });

        branchStock.setQuantity(branchStock.getQuantity() + quantity);
        branchStockRepository.save(branchStock);
    }

    private void createMovement(Branch branch, Product product, Integer quantity, String reference, String supplierName) {
        StockMovement movement = new StockMovement();
        movement.setBranch(branch);
        movement.setProduct(product);
        movement.setMovementType(MovementType.PURCHASE);
        movement.setQuantity(quantity);
        movement.setReference(reference);
        movement.setNotes("Purchase from supplier " + supplierName);
        movement.setOccurredAt(LocalDateTime.now());
        stockMovementRepository.save(movement);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private PurchaseResponse toResponse(Purchase purchase) {
        List<PurchaseItemResponse> itemResponses = new ArrayList<>(
            purchase.getItems().stream()
                .sorted(Comparator.comparing(PurchaseItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> new PurchaseItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getSku(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitPurchasePrice(),
                    item.getSubtotal()
                ))
                .toList()
        );

        return new PurchaseResponse(
            purchase.getId(),
            purchase.getPurchaseNumber(),
            purchase.getSupplierName(),
            purchase.getPurchaseDate(),
            purchase.getBranch().getId(),
            purchase.getBranch().getCode(),
            purchase.getBranch().getName(),
            purchase.getTotalAmount(),
            purchase.getNotes(),
            itemResponses
        );
    }
}
