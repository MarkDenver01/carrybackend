package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.driver.DriverRequest;
import com.carry_guide.carry_guide_admin.model.entity.Driver;
import com.carry_guide.carry_guide_admin.presentation.handler.ValidationException;
import com.carry_guide.carry_guide_admin.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
public class DriverController {

    @Autowired
    DriverService driverService;

    // 🔥 Driver registration endpoint
    @PostMapping(value = "/api/driver/register", consumes = "multipart/form-data")
    public Driver registerDriver(@ModelAttribute DriverRequest request) {
        return driverService.registerDriver(request);
    }
}
