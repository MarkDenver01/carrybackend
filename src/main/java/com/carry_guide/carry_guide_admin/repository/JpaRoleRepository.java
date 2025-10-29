package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.dto.enums.RoleState;
import com.carry_guide.carry_guide_admin.model.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByRoleState(RoleState roleState);
}
