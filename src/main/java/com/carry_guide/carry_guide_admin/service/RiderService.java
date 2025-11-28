package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.RiderStatus;
import com.carry_guide.carry_guide_admin.model.entity.Rider;
import com.carry_guide.carry_guide_admin.repository.JpaRiderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final JpaRiderRepository riderRepo;

    public List<Rider> getAll() {
        return riderRepo.findAll();
    }

    public Rider assignRider(Long riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found"));

        rider.setStatus(RiderStatus.ON_DELIVERY);
        rider.setOrdersToday(rider.getOrdersToday() + 1);
        rider.setWorkload(rider.getOrdersToday() * 10);
        rider.setLastAssigned(LocalDateTime.now());

        return riderRepo.save(rider);
    }

    public Rider completeDelivery(Long riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found"));

        rider.setStatus(RiderStatus.AVAILABLE);
        rider.setCompletedDeliveries(rider.getCompletedDeliveries() + 1);
        rider.setWorkload(Math.max(0, rider.getWorkload() - 10));
        rider.setLastActive(LocalDateTime.now());

        return riderRepo.save(rider);
    }
}
