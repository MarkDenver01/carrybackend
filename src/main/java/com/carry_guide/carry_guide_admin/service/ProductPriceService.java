package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceDTO;
import com.carry_guide.carry_guide_admin.dto.request.product.ProductPriceMapper;
import com.carry_guide.carry_guide_admin.model.entity.Product;
import com.carry_guide.carry_guide_admin.model.entity.ProductPrice;
import com.carry_guide.carry_guide_admin.repository.JpaProductPriceRepository;
import com.carry_guide.carry_guide_admin.repository.JpaProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductPriceService {

    private final JpaProductPriceRepository productPriceRepository;
    private final JpaProductRepository productRepository;
    private final ProductPriceMapper mapper;

    public List<ProductPriceDTO> getAll() {
        return productPriceRepository.findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public ProductPriceDTO save(ProductPriceDTO dto) {
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        ProductPrice price = ProductPrice.builder()
                .priceId(dto.getPriceId())
                .product(product)
                .basePrice(dto.getBasePrice())
                .effectiveDate(dto.getEffectiveDate())
                .build();

        return mapper.toDto(productPriceRepository.save(price));
    }

    public void delete(Long id) {
        productPriceRepository.deleteById(id);
    }
}
