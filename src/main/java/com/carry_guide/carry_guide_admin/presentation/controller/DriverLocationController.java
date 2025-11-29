package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.tracker.DriverLocationUpdateRequest;
import com.carry_guide.carry_guide_admin.dto.response.tracker.DriverLocation;
import com.carry_guide.carry_guide_admin.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverLocationController {
    private final DriverLocationService driverLocationService;

    // mobile → backend
    @PostMapping("/location")
    public ResponseEntity<Void> updateLocation(@RequestBody DriverLocationUpdateRequest request) {
        driverLocationService.updateLocation(request);
        return ResponseEntity.ok().build();
    }

    // dashboard initial fetch
    @GetMapping("/{driverId}/location")
    public ResponseEntity<DriverLocation> getLatest(@PathVariable String driverId) {
        DriverLocation location = driverLocationService.getLatestLocation(driverId);
        if (location == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(location);
    }

    // dashboard live stream (SSE)
    @GetMapping(value = "/{driverId}/location/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@PathVariable String driverId) {
        return driverLocationService.subscribe(driverId);
    }
}
