package com.carry_guide.carry_guide_admin.domain.repository;

import com.carry_guide.carry_guide_admin.infrastructure.persistence.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByMobileNumber(String mobileNumber);

    Optional<User> findByEmailAndDriver_DriversLicenseNumber(String email, String driversLicenseId);

    Boolean existsByEmail(String email);

    Boolean existsByEmailAndSignupMethod(String email, String signupMethod);
}
