package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.driver.DriverRequest;
import com.carry_guide.carry_guide_admin.model.entity.Driver;
import com.carry_guide.carry_guide_admin.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/driver")
public class DriverController {

    private final DriverService driverService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public Driver registerDriver(@ModelAttribute DriverRequest request) {
        try {
            return driverService.registerDriver(request);
        } catch (MultipartException e) {
            throw new RuntimeException("Invalid files uploaded.");
        }
    }
}
