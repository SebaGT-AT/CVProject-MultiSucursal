package com.multisucursal.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.multisucursal.inventory.branch.entity.Branch;
import com.multisucursal.inventory.branch.repository.BranchRepository;
import com.multisucursal.inventory.product.entity.Category;
import com.multisucursal.inventory.product.entity.Product;
import com.multisucursal.inventory.product.repository.CategoryRepository;
import com.multisucursal.inventory.product.repository.ProductRepository;
import com.multisucursal.inventory.stock.entity.BranchStock;
import com.multisucursal.inventory.stock.entity.MovementType;
import com.multisucursal.inventory.stock.entity.StockMovement;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import com.multisucursal.inventory.stock.repository.StockMovementRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class InventoryRepositoryIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BranchRepository branchRepository;

    @Autowired
    private BranchStockRepository branchStockRepository;

    @Autowired
    private StockMovementRepository stockMovementRepository;

    @Test
    void shouldPersistBaseInventoryModel() {
        Category category = new Category();
        category.setName("Laptops");
        category.setDescription("Portable computing devices");
        category = categoryRepository.save(category);

        Product product = new Product();
        product.setSku("LAP-001");
        product.setName("Lenovo ThinkPad E14");
        product.setDescription("Business laptop");
        product.setPurchasePrice(new BigDecimal("550.00"));
        product.setSalePrice(new BigDecimal("720.00"));
        product.setCategory(category);
        product = productRepository.save(product);

        Branch branch = new Branch();
        branch.setCode("SCL-CENTRO");
        branch.setName("Sucursal Centro");
        branch.setCity("Santiago");
        branch.setAddress("Av. Libertador 123");
        branch.setManagerName("Maria Perez");
        branch.setPhone("+56 9 1234 5678");
        branch.setEmail("centro@multisucursal.dev");
        branch = branchRepository.save(branch);

        BranchStock branchStock = new BranchStock();
        branchStock.setBranch(branch);
        branchStock.setProduct(product);
        branchStock.setQuantity(15);
        branchStock.setMinimumStock(5);
        branchStockRepository.save(branchStock);

        StockMovement stockMovement = new StockMovement();
        stockMovement.setBranch(branch);
        stockMovement.setProduct(product);
        stockMovement.setMovementType(MovementType.INITIAL_LOAD);
        stockMovement.setQuantity(15);
        stockMovement.setReference("INIT-LOAD-001");
        stockMovement.setNotes("Initial inventory setup");
        stockMovement.setOccurredAt(LocalDateTime.now());
        stockMovementRepository.save(stockMovement);

        assertThat(categoryRepository.existsByNameIgnoreCase("laptops")).isTrue();
        assertThat(productRepository.findBySkuIgnoreCase("lap-001")).isPresent();
        assertThat(branchRepository.findByCodeIgnoreCase("scl-centro")).isPresent();
        assertThat(branchStockRepository.findByBranchIdAndProductId(branch.getId(), product.getId()))
            .isPresent()
            .get()
            .extracting(BranchStock::getQuantity)
            .isEqualTo(15);
        assertThat(stockMovementRepository.findByProductIdOrderByOccurredAtDesc(product.getId()))
            .hasSize(1)
            .first()
            .extracting(StockMovement::getMovementType)
            .isEqualTo(MovementType.INITIAL_LOAD);
    }
}

