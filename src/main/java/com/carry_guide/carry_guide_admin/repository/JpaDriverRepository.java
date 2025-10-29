package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaDriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByUser_UserId(Long userId);
}
