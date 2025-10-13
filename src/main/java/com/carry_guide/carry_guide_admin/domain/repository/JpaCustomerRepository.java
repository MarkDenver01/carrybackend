package com.carry_guide.carry_guide_admin.domain.repository;

import com.carry_guide.carry_guide_admin.infrastructure.persistence.entity.Admin;
import com.carry_guide.carry_guide_admin.infrastructure.persistence.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser_UserId(Long userId);
}
