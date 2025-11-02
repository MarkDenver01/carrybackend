package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Tax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaTaxRepository extends JpaRepository<Tax, Integer> {

    /**
     * Finds all currently active tax rates.
     */
    Optional<Tax> findByEffectiveFromBeforeAndEffectiveToAfter(LocalDateTime from, LocalDateTime to);

    /**
     * Finds all tax rates that are active using inclusive comparison.
     */
    List<Tax> findByEffectiveFromLessThanEqualAndEffectiveToGreaterThanEqual(LocalDateTime now1, LocalDateTime now2);
}
