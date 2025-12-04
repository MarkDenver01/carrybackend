package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.enums.MembershipStatus;
import com.carry_guide.carry_guide_admin.model.entity.Membership;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JpaMembershipRepository extends JpaRepository<Membership, Long> {

    Optional<Membership> findByCustomer_CustomerId(Long customerId);

    long countByStatus(MembershipStatus status);

    @Query("""
        SELECT COUNT(m)
        FROM Membership m
        WHERE m.startDate >= :from
    """)
    long countNewMembersSince(LocalDate from);

    @Query("""
        SELECT COUNT(m)
        FROM Membership m
        WHERE m.expiryDate BETWEEN :start AND :end
    """)
    long countExpiringBetween(LocalDate start, LocalDate end);

    @Query("""
        SELECT m
        FROM Membership m
        JOIN FETCH m.customer c
        ORDER BY m.pointsBalance DESC
    """)
    List<Membership> findTopMembers(Pageable pageable);

    @Query("""
        SELECT m
        FROM Membership m
        JOIN FETCH m.customer c
        ORDER BY c.userName ASC
    """)
    List<Membership> findAllWithCustomer();
}