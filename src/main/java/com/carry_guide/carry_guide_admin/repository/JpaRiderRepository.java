package com.carry_guide.carry_guide_admin.repository;

import com.carry_guide.carry_guide_admin.model.entity.Rider;
import com.carry_guide.carry_guide_admin.domain.enums.RiderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JpaRiderRepository extends JpaRepository<Rider, Long> {

    List<Rider> findByStatus(RiderStatus status);
    long countByStatus(RiderStatus status);

}
