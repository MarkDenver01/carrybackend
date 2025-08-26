package com.carry_guide.carry_guide_admin.jwt.repository;

import com.carry_guide.carry_guide_admin.jwt.model.entity.Role;
import com.carry_guide.carry_guide_admin.jwt.model.state.RoleState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findRoleByRoleState(RoleState roleState);
}
