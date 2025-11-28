package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.RiderStatus;
import com.carry_guide.carry_guide_admin.dto.request.driver.DriverRequest;
import com.carry_guide.carry_guide_admin.model.entity.Rider;
import com.carry_guide.carry_guide_admin.repository.JpaRiderRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final JpaRiderRepository riderRepo;

    private final String UPLOAD_DIR = "uploads/drivers/";

    private String saveFile(MultipartFile file, String prefix) {
        if (file == null || file.isEmpty()) return null;

        try {
            String fileName = prefix + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR + fileName);

            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());

            return "/uploads/drivers/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("File upload failed: " + prefix);
        }
    }

    // ============================================
    //   REGISTER NEW DRIVER  ← THE IMPORTANT PART
    // ============================================
    public Rider registerDriver(DriverRequest req) {

        Rider rider = new Rider();
        rider.setName(req.getUserName());
        rider.setEmail(req.getEmail());
        rider.setContact(req.getMobileNumber());
        rider.setAddress(req.getAddress());
        rider.setDriversLicenseNumber(req.getDriversLicenseNumber());
        rider.setStatus(RiderStatus.AVAILABLE);
        rider.setOrdersToday(0);
        rider.setCompletedDeliveries(0);
        rider.setWorkload(0);
        rider.setLastAssigned(null);
        rider.setLastActive(LocalDateTime.now());
        rider.setHomeBase(req.getAddress());

        // Save images
        rider.setPhotoUrl(saveFile(req.getPhotoFile(), "photo"));
        rider.setFrontIdUrl(saveFile(req.getFrontIdFile(), "front"));
        rider.setBackIdUrl(saveFile(req.getBackIdFile(), "back"));

        return riderRepo.save(rider);
    }

    // =====================================================
    // GET ALL DRIVERS
    // =====================================================
    public List<Rider> getAll() {
        return riderRepo.findAll();
    }

    public Rider assignRider(Long riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found: " + riderId));

        rider.setStatus(RiderStatus.ON_DELIVERY);
        rider.setOrdersToday(rider.getOrdersToday() + 1);
        rider.setWorkload(rider.getOrdersToday() * 10);
        rider.setLastAssigned(LocalDateTime.now());
        rider.setLastActive(LocalDateTime.now());

        return riderRepo.save(rider);
    }

    public Rider completeDelivery(Long riderId) {
        Rider rider = riderRepo.findById(riderId)
                .orElseThrow(() -> new EntityNotFoundException("Rider not found: " + riderId));

        rider.setStatus(RiderStatus.AVAILABLE);
        rider.setCompletedDeliveries(rider.getCompletedDeliveries() + 1);
        rider.setWorkload(Math.max(0, rider.getWorkload() - 10));
        rider.setLastActive(LocalDateTime.now());

        return riderRepo.save(rider);
    }
    public void deleteRider(Long riderId) {
        if (!riderRepo.existsById(riderId)) {
            throw new EntityNotFoundException("Rider not found");
        }
        riderRepo.deleteById(riderId);
    }

}
