package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.service.ProductPriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/api/prices")
@RequiredArgsConstructor
public class ProductPriceController {

    ProductPriceService productPriceService;

    @GetMapping
    public List<ProductPriceDTO> getAll() {
        return productPriceService.getAll();
    }

    @PostMapping
    public ProductPriceDTO add(@RequestBody ProductPriceDTO dto) {
        return productPriceService.save(dto);
    }

    @PutMapping("/{id}")
    public ProductPriceDTO update(@PathVariable Long id,
                                  @RequestBody ProductPriceDTO dto) {
        dto.setPriceId(id);
        return productPriceService.save(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productPriceService.delete(id);
    }
}
