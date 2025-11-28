package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.driver.DriverRequest;
import com.carry_guide.carry_guide_admin.model.entity.Rider;
import com.carry_guide.carry_guide_admin.service.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/driver")
public class DriverController {

    private final RiderService riderService;

    @PostMapping(value = "/register", consumes = "multipart/form-data")
    public ResponseEntity<Rider> registerDriver(@ModelAttribute DriverRequest request) {
        try {
            Rider saved = riderService.registerDriver(request);
            return ResponseEntity.ok(saved);
        } catch (MultipartException e) {
            throw new RuntimeException("Invalid files uploaded.");
        }
    }
}
