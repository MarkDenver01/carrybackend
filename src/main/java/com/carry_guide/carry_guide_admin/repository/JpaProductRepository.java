package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByProductStatus(String productStatus);

    // ✅ Corrected to match Product entity field names
    List<Product> findByProductNameContainingIgnoreCase(String keyword);

    // ✅ Traverse category relationship
    List<Product> findByCategory_CategoryNameContainingIgnoreCase(String keyword);

    List<Product> findTop20ByOrderByProductIdDesc();
}
