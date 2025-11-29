package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.tracker.DriverLocationUpdateRequest;
import com.carry_guide.carry_guide_admin.dto.response.tracker.DriverLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
public class DriverLocationService {

    // latest location per driver
    private final Map<String, DriverLocation> latestLocations = new ConcurrentHashMap<>();

    // SSE subscribers per driver
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public void updateLocation(DriverLocationUpdateRequest req) {
        DriverLocation location = new DriverLocation(
                req.getDriverId(),
                req.getLatitude(),
                req.getLongitude(),
                req.getAccuracy(),
                req.getTimestamp()
        );

        latestLocations.put(req.getDriverId(), location);

        // broadcast to all connected dashboard clients
        List<SseEmitter> driverEmitters = emitters.getOrDefault(req.getDriverId(), List.of());
        for (SseEmitter emitter : driverEmitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name("location")
                        .data(location));
            } catch (IOException e) {
                log.warn("Failed to send SSE, removing emitter: {}", e.getMessage());
                emitter.complete();
            }
        }
    }

    public SseEmitter subscribe(String driverId) {
        SseEmitter emitter = new SseEmitter(0L); // no timeout

        emitters.computeIfAbsent(driverId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(driverId, emitter));
        emitter.onTimeout(() -> removeEmitter(driverId, emitter));
        emitter.onError((e) -> removeEmitter(driverId, emitter));

        // send current location immediately if we have one
        DriverLocation current = latestLocations.get(driverId);
        if (current != null) {
            try {
                emitter.send(SseEmitter.event()
                        .name("location")
                        .data(current));
            } catch (IOException e) {
                log.warn("Failed to send initial location for driver {}: {}", driverId, e.getMessage());
            }
        }

        return emitter;
    }

    private void removeEmitter(String driverId, SseEmitter emitter) {
        List<SseEmitter> driverEmitters = emitters.get(driverId);
        if (driverEmitters != null) {
            driverEmitters.remove(emitter);
        }
    }

    public DriverLocation getLatestLocation(String driverId) {
        return latestLocations.get(driverId);
    }
}
