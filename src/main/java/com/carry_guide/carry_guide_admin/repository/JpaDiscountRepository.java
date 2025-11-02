package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Discount;
import com.carry_guide.carry_guide_admin.model.enums.DiscountCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaDiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByDiscountCategory(DiscountCategory category);
}
