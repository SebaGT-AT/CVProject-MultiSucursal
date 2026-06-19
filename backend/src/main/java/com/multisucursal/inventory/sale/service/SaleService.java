package com.multisucursal.inventory.sale.service;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.branch.repository.BranchRepository;
import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.product.entity.Product;
import com.multisucursal.inventory.product.repository.ProductRepository;
import com.multisucursal.inventory.sale.dto.SaleItemRequest;
import com.multisucursal.inventory.sale.dto.SaleItemResponse;
import com.multisucursal.inventory.sale.dto.SaleRequest;
import com.multisucursal.inventory.sale.dto.SaleResponse;
import com.multisucursal.inventory.sale.entity.Sale;
import com.multisucursal.inventory.sale.entity.SaleItem;
import com.multisucursal.inventory.sale.repository.SaleRepository;
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
public class SaleService {

    private final SaleRepository saleRepository;
    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAllByOrderBySaleDateDescIdDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<SaleResponse> findByBranch(Long branchId) {
        ensureBranchExists(branchId);
        return saleRepository.findByBranchIdOrderBySaleDateDescIdDesc(branchId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public SaleResponse findById(Long id) {
        return toResponse(getSaleById(id));
    }

    @Transactional
    public SaleResponse create(SaleRequest request) {
        validateUniqueSaleNumber(request.saleNumber(), null);
        Branch branch = getBranchById(request.branchId());

        Sale sale = new Sale();
        sale.setSaleNumber(request.saleNumber().trim().toUpperCase());
        sale.setCustomerName(request.customerName().trim());
        sale.setSaleDate(request.saleDate());
        sale.setBranch(branch);
        sale.setNotes(trimToNull(request.notes()));

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleItemRequest itemRequest : request.items()) {
            Product product = getProductById(itemRequest.productId());
            BranchStock branchStock = getExistingBranchStock(branch.getId(), product.getId());
            ensureSufficientStock(branchStock, itemRequest.quantity(), product.getSku(), branch.getCode());

            SaleItem item = buildSaleItem(sale, product, itemRequest);
            sale.getItems().add(item);
            totalAmount = totalAmount.add(item.getSubtotal());

            branchStock.setQuantity(branchStock.getQuantity() - itemRequest.quantity());
            branchStockRepository.save(branchStock);
            createMovement(branch, product, itemRequest.quantity(), sale.getSaleNumber(), sale.getCustomerName());
        }

        sale.setTotalAmount(totalAmount);
        return toResponse(saleRepository.save(sale));
    }

    private Sale getSaleById(Long id) {
        return saleRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Sale not found with id " + id));
    }

    private Branch getBranchById(Long branchId) {
        return branchRepository.findById(branchId)
            .orElseThrow(() -> new ResourceNotFoundException("Branch not found with id " + branchId));
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + productId));
    }

    private BranchStock getExistingBranchStock(Long branchId, Long productId) {
        return branchStockRepository.findByBranchIdAndProductId(branchId, productId)
            .orElseThrow(() -> new IllegalStateException("Branch has no stock for the selected product"));
    }

    private void ensureBranchExists(Long branchId) {
        if (!branchRepository.existsById(branchId)) {
            throw new ResourceNotFoundException("Branch not found with id " + branchId);
        }
    }

    private void validateUniqueSaleNumber(String saleNumber, Long currentId) {
        saleRepository.findBySaleNumberIgnoreCase(saleNumber.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Sale number is already in use");
            });
    }

    private void ensureSufficientStock(BranchStock branchStock, Integer requestedQuantity, String sku, String branchCode) {
        if (branchStock.getQuantity() < requestedQuantity) {
            throw new IllegalStateException("Insufficient stock for product " + sku + " in branch " + branchCode);
        }
    }

    private SaleItem buildSaleItem(Sale sale, Product product, SaleItemRequest request) {
        SaleItem item = new SaleItem();
        item.setSale(sale);
        item.setProduct(product);
        item.setQuantity(request.quantity());
        item.setUnitSalePrice(request.unitSalePrice());
        item.setSubtotal(request.unitSalePrice().multiply(BigDecimal.valueOf(request.quantity())));
        return item;
    }

    private void createMovement(Branch branch, Product product, Integer quantity, String reference, String customerName) {
        StockMovement movement = new StockMovement();
        movement.setBranch(branch);
        movement.setProduct(product);
        movement.setMovementType(MovementType.SALE);
        movement.setQuantity(quantity);
        movement.setReference(reference);
        movement.setNotes("Sale to customer " + customerName);
        movement.setOccurredAt(LocalDateTime.now());
        stockMovementRepository.save(movement);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private SaleResponse toResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = new ArrayList<>(
            sale.getItems().stream()
                .sorted(Comparator.comparing(SaleItem::getId, Comparator.nullsLast(Long::compareTo)))
                .map(item -> new SaleItemResponse(
                    item.getId(),
                    item.getProduct().getId(),
                    item.getProduct().getSku(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getUnitSalePrice(),
                    item.getSubtotal()
                ))
                .toList()
        );

        return new SaleResponse(
            sale.getId(),
            sale.getSaleNumber(),
            sale.getCustomerName(),
            sale.getSaleDate(),
            sale.getBranch().getId(),
            sale.getBranch().getCode(),
            sale.getBranch().getName(),
            sale.getTotalAmount(),
            sale.getNotes(),
            itemResponses
        );
    }
}
