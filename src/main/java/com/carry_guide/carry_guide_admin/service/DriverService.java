package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.driver.DriverRequest;
import com.carry_guide.carry_guide_admin.model.entity.Driver;
import com.carry_guide.carry_guide_admin.repository.JpaDriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DriverService {

    @Autowired
    JpaDriverRepository driverRepository;

    @Autowired
    FileStorageService fileStorageService;

    public Driver registerDriver(DriverRequest request) {

        // Check duplicate mobile number
        if (driverRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new RuntimeException("Mobile number already exists!");
        }

        Driver driver = new Driver();
        driver.setUserName(request.getUserName());
        driver.setEmail(request.getEmail());
        driver.setAddress(request.getAddress());
        driver.setMobileNumber(request.getMobileNumber());
        driver.setDriversLicenseNumber(request.getDriversLicenseNumber());

        // Save images
        driver.setPhotoUrl(fileStorageService.saveDriverFile(request.getPhotoFile()));
        driver.setFrontIdUrl(fileStorageService.saveDriverFile(request.getFrontIdFile()));
        driver.setBackIdUrl(fileStorageService.saveDriverFile(request.getBackIdFile()));

        return driverRepository.save(driver);
    }
}
