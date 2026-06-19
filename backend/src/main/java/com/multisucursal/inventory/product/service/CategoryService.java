package com.multisucursal.inventory.product.service;

import com.multisucursal.inventory.exception.ResourceNotFoundException;
import com.multisucursal.inventory.product.dto.CategoryRequest;
import com.multisucursal.inventory.product.dto.CategoryResponse;
import com.multisucursal.inventory.product.entity.Category;
import com.multisucursal.inventory.product.repository.CategoryRepository;
import com.multisucursal.inventory.product.repository.ProductRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        return toResponse(getCategoryById(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        validateUniqueName(request.name(), null);

        Category category = new Category();
        applyRequest(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getCategoryById(id);
        validateUniqueName(request.name(), id);

        applyRequest(category, request);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategoryById(id);
        if (productRepository.existsByCategoryId(id)) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }
        categoryRepository.delete(category);
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + id));
    }

    private void validateUniqueName(String name, Long currentId) {
        categoryRepository.findByNameIgnoreCase(name.trim())
            .filter(existing -> !existing.getId().equals(currentId))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Category name is already in use");
            });
    }

    private void applyRequest(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setDescription(request.description());
        category.setActive(request.active());
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.isActive(),
            category.getProducts().size()
        );
    }
}

