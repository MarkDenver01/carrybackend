package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.model.entity.Rider;
import com.carry_guide.carry_guide_admin.service.RiderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/public/api/riders")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @GetMapping("/all")
    public ResponseEntity<List<Rider>> getAllRiders() {
        return ResponseEntity.ok(riderService.getAll());
    }

    @PutMapping("/{riderId}/assign")
    public ResponseEntity<Rider> assignRider(@PathVariable Long riderId) {
        return ResponseEntity.ok(riderService.assignRider(riderId));
    }

    @PutMapping("/{riderId}/complete")
    public ResponseEntity<Rider> completeDelivery(@PathVariable Long riderId) {
        return ResponseEntity.ok(riderService.completeDelivery(riderId));
    }
}