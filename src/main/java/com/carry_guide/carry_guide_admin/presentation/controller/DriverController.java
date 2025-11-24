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

        System.out.println("🔥🔥🔥 CONTROLLER HIT: /admin/api/driver/register");
        System.out.println("userName = " + request.getUserName());
        System.out.println("email = " + request.getEmail());
        System.out.println("mobile = " + request.getMobileNumber());
        System.out.println("photoFile = " + (request.getPhotoFile() != null ? request.getPhotoFile().getOriginalFilename() : "NULL"));
        System.out.println("frontIdFile = " + (request.getFrontIdFile() != null ? request.getFrontIdFile().getOriginalFilename() : "NULL"));
        System.out.println("backIdFile = " + (request.getBackIdFile() != null ? request.getBackIdFile().getOriginalFilename() : "NULL"));

        return driverService.registerDriver(request);
    }
}
