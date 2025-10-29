package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.dto.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.model.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaAdminRepository extends JpaRepository<Admin, Long> {
    Optional<Admin> findByUser_UserId(Long userId);
    Optional<Admin> findByAdminIdAndAccountStatus(Long adminId, AccountStatus accountStatus);
}
