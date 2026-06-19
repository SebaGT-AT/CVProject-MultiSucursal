package com.multisucursal.inventory.dashboard.service;

import com.multisucursal.inventory.dashboard.dto.DashboardLowStockResponse;
import com.multisucursal.inventory.dashboard.dto.DashboardMetricResponse;
import com.multisucursal.inventory.dashboard.dto.DashboardSummaryResponse;
import com.multisucursal.inventory.purchase.repository.PurchaseRepository;
import com.multisucursal.inventory.sale.repository.SaleRepository;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BranchStockRepository branchStockRepository;
    private final SaleRepository saleRepository;
    private final PurchaseRepository purchaseRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        List<DashboardLowStockResponse> lowStockProducts = branchStockRepository.findAllByQuantityLessThanEqualMinimumStock()
            .stream()
            .map(stock -> new DashboardLowStockResponse(
                stock.getBranch().getId(),
                stock.getBranch().getCode(),
                stock.getBranch().getName(),
                stock.getProduct().getId(),
                stock.getProduct().getSku(),
                stock.getProduct().getName(),
                stock.getQuantity(),
                stock.getMinimumStock()
            ))
            .toList();

        DashboardMetricResponse salesToday = new DashboardMetricResponse(
            saleRepository.countBySaleDate(today),
            saleRepository.sumTotalAmountBySaleDate(today).orElse(BigDecimal.ZERO)
        );

        DashboardMetricResponse purchasesThisMonth = new DashboardMetricResponse(
            purchaseRepository.countByPurchaseDateBetween(monthStart, monthEnd),
            purchaseRepository.sumTotalAmountByPurchaseDateBetween(monthStart, monthEnd).orElse(BigDecimal.ZERO)
        );

        return new DashboardSummaryResponse(today, salesToday, purchasesThisMonth, lowStockProducts);
    }
}
