package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaCustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUser_UserId(Long userId);

    Optional<Customer> findByUser(User user);

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    Optional<Customer> findByMobileNumberOrEmail(String mobileNumber, String email);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByMobileNumber(String mobileNumber);

    long countByUserAccountStatus(AccountStatus userAccountStatus);

    long countByCreatedDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT FUNCTION('DATE_TRUNC','month', c.createdDate) AS month,
           COUNT(c)
    FROM Customer c
    WHERE c.createdDate IS NOT NULL
    GROUP BY FUNCTION('DATE_TRUNC','month', c.createdDate)
    ORDER BY FUNCTION('DATE_TRUNC','month', c.createdDate)
""")
    List<Object[]> countRegistrationsPerMonth();

}