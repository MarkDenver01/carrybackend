package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.tracker.DriverLocationUpdateRequest;
import com.carry_guide.carry_guide_admin.dto.response.tracker.DriverLocation;
import com.carry_guide.carry_guide_admin.service.DriverLocationService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
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
        SseEmitter emitter = new SseEmitter(0L);

        HttpServletResponse res = ((ServletRequestAttributes) RequestContextHolder
                .getRequestAttributes()).getResponse();

        if (res != null) {
            res.setHeader("Cache-Control", "no-cache");
            res.setHeader("X-Accel-Buffering", "no");
            res.setHeader("Connection", "keep-alive");
            res.setHeader("Content-Type", "text/event-stream");
            res.setHeader("Access-Control-Allow-Origin", "https://capstone.wrapandcarry.com");
            res.setHeader("Access-Control-Allow-Credentials", "true");
        }

        driverLocationService.subscribe(driverId, emitter);

        try {
            emitter.send(SseEmitter.event().name("init").data("connected"));
        } catch (Exception ignored) {}

        return emitter; // 🔥 RETURN SAME EMITTER
    }
}
