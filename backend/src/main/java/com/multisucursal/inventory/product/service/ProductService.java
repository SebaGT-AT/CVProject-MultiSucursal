package com.multisucursal.inventory.product.service;

import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.product.dto.ProductRequest;
import com.multisucursal.inventory.product.dto.ProductResponse;
import com.multisucursal.inventory.product.entity.Category;
import com.multisucursal.inventory.product.entity.Product;
import com.multisucursal.inventory.product.repository.CategoryRepository;
import com.multisucursal.inventory.product.repository.ProductRepository;
import com.multisucursal.inventory.stock.repository.BranchStockRepository;
import com.multisucursal.inventory.stock.repository.StockMovementRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BranchStockRepository branchStockRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        return toResponse(getProductById(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        validateUniqueSku(request.sku(), null);
        Category category = getCategoryById(request.categoryId());

        Product product = new Product();
        applyRequest(product, request, category);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getProductById(id);
        validateUniqueSku(request.sku(), id);
        Category category = getCategoryById(request.categoryId());

        applyRequest(product, request, category);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = getProductById(id);
        if (branchStockRepository.existsByProductId(id) || stockMovementRepository.existsByProductId(id)) {
            throw new IllegalStateException("Cannot delete product with stock history");
        }
        productRepository.delete(product);
    }

    private Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found with id " + id));
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private void validateUniqueSku(String sku, Long currentId) {
        productRepository.findBySkuIgnoreCase(sku.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("SKU is already in use");
            });
    }

    private void applyRequest(Product product, ProductRequest request, Category category) {
        product.setSku(request.sku().trim().toUpperCase());
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPurchasePrice(request.purchasePrice());
        product.setSalePrice(request.salePrice());
        product.setActive(request.active());
        product.setCategory(category);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getSku(),
            product.getName(),
            product.getDescription(),
            product.getPurchasePrice(),
            product.getSalePrice(),
            product.isActive(),
            product.getCategory().getId(),
            product.getCategory().getName()
        );
    }
}

