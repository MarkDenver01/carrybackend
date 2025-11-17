package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductCategoryDTO;
import com.carry_guide.carry_guide_admin.model.entity.ProductCategory;
import com.carry_guide.carry_guide_admin.repository.JpaProductCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductCategoryService {
    @Autowired
    JpaProductCategoryRepository categoryRepository;

    public List<ProductCategoryDTO> getAll() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public ProductCategoryDTO getById(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return toDTO(category);
    }

    public ProductCategoryDTO create(ProductCategoryDTO dto) {
        if (categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName())) {
            throw new RuntimeException("Category name already exists: " + dto.getCategoryName());
        }
        ProductCategory category = new ProductCategory();
        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());
        ProductCategory saved = categoryRepository.save(category);
        return toDTO(saved);
    }

    public ProductCategoryDTO update(Long id, ProductCategoryDTO dto) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // If nagbago ang name, check duplication
        if (!category.getCategoryName().equalsIgnoreCase(dto.getCategoryName())
                && categoryRepository.existsByCategoryNameIgnoreCase(dto.getCategoryName())) {
            throw new RuntimeException("Category name already exists: " + dto.getCategoryName());
        }

        category.setCategoryName(dto.getCategoryName());
        category.setCategoryDescription(dto.getCategoryDescription());

        ProductCategory updated = categoryRepository.save(category);
        return toDTO(updated);
    }

    public void delete(Long id) {
        ProductCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Optional: prevent delete if may products
        if (!category.getProducts().isEmpty()) {
            throw new RuntimeException("Cannot delete category, there are products linked.");
        }

        categoryRepository.delete(category);
    }

    private ProductCategoryDTO toDTO(ProductCategory category) {
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setCategoryId(category.getCategoryId());
        dto.setCategoryName(category.getCategoryName());
        dto.setCategoryDescription(category.getCategoryDescription());
        return dto;
    }
}
