package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.UserHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaUserHistoryRepository extends JpaRepository<UserHistory, Long> {
    List<UserHistory> findByCustomerId(Long customerId);

    boolean existsByCustomerIdAndProductKeyword(Long customerId, String productKeyword);

    List<UserHistory> findByCustomerIdOrderByDateTimeDesc(Long customerId);
}
