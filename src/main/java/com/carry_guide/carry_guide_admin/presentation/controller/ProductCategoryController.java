package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductCategoryDTO;
import com.carry_guide.carry_guide_admin.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/product-categories")
@RequiredArgsConstructor
public class ProductCategoryController {

    @Autowired
    ProductCategoryService categoryService;

    @GetMapping
    public List<ProductCategoryDTO> getAll() {
        return categoryService.getAll();
    }

    @GetMapping("/{id}")
    public ProductCategoryDTO getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @PostMapping
    public ProductCategoryDTO create(@RequestBody ProductCategoryDTO dto) {
        return categoryService.create(dto);
    }

    @PutMapping("/{id}")
    public ProductCategoryDTO update(@PathVariable Long id,
                                     @RequestBody ProductCategoryDTO dto) {
        return categoryService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
