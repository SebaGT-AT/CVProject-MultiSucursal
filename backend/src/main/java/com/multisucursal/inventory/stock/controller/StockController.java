package com.multisucursal.inventory.stock.controller;

import com.multisucursal.inventory.stock.dto.BranchStockResponse;
import com.multisucursal.inventory.stock.dto.StockAdjustmentRequest;
import com.multisucursal.inventory.stock.dto.StockMovementResponse;
import com.multisucursal.inventory.stock.dto.StockTransferRequest;
import com.multisucursal.inventory.stock.service.StockService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<BranchStockResponse>> findAllStocks() {
        return ResponseEntity.ok(stockService.findAllStocks());
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<BranchStockResponse>> findStocksByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(stockService.findStocksByBranch(branchId));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<BranchStockResponse>> findStocksByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findStocksByProduct(productId));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<StockMovementResponse>> findAllMovements() {
        return ResponseEntity.ok(stockService.findAllMovements());
    }

    @GetMapping("/movements/branch/{branchId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<StockMovementResponse>> findMovementsByBranch(@PathVariable Long branchId) {
        return ResponseEntity.ok(stockService.findMovementsByBranch(branchId));
    }

    @GetMapping("/movements/product/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<StockMovementResponse>> findMovementsByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findMovementsByProduct(productId));
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BranchStockResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockService.adjustStock(request));
    }

    @PostMapping("/transfers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> transferStock(@Valid @RequestBody StockTransferRequest request) {
        stockService.transferStock(request);
        return ResponseEntity.ok().build();
    }
}

