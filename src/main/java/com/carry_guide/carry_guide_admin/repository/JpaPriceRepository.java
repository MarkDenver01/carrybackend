package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaPriceRepository extends JpaRepository<Price, Integer> {
    Optional<Price> findByProduct_ProductId(int productId);
}
