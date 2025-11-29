package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.tracker.DriverLocationUpdateRequest;
import com.carry_guide.carry_guide_admin.dto.response.tracker.DriverLocation;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private final Map<String, DriverLocation> latestLocations = new ConcurrentHashMap<>();

    // MULTIPLE subscribers per driver
    private final Map<String, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final ObjectMapper mapper = new ObjectMapper();

    public void updateLocation(DriverLocationUpdateRequest req) {

        DriverLocation location = new DriverLocation(
                req.getDriverId(),
                req.getLatitude(),
                req.getLongitude(),
                req.getAccuracy(),
                req.getTimestamp()
        );

        latestLocations.put(req.getDriverId(), location);

        List<SseEmitter> subscribers =
                emitters.getOrDefault(req.getDriverId(), List.of());

        String json;
        try {
            json = mapper.writeValueAsString(location);
        } catch (Exception e) {
            log.error("Failed to json stringify: {}", e.getMessage());
            return;
        }

        for (SseEmitter emitter : subscribers) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("location")
                                .data(json)              // 🔥 SEND JSON STRING
                                .reconnectTime(1000)
                );
            } catch (IOException e) {
                log.warn("Emitter dead, removing {}", e.getMessage());
                emitter.complete();
                removeEmitter(req.getDriverId(), emitter);
            }
        }
    }

    public SseEmitter subscribe(String driverId) {
        SseEmitter emitter = new SseEmitter(0L);

        emitters.computeIfAbsent(driverId, id -> new CopyOnWriteArrayList<>())
                .add(emitter);

        emitter.onCompletion(() -> removeEmitter(driverId, emitter));
        emitter.onTimeout(() -> removeEmitter(driverId, emitter));
        emitter.onError(e -> removeEmitter(driverId, emitter));

        DriverLocation current = latestLocations.get(driverId);

        if (current != null) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("location")
                                .data(mapper.writeValueAsString(current)) // 🔥 JSON
                                .reconnectTime(1000)
                );
            } catch (Exception ignored) {}
        }

        return emitter;
    }

    private void removeEmitter(String driverId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(driverId);
        if (list != null) list.remove(emitter);
    }

    public DriverLocation getLatestLocation(String driverId) {
        return latestLocations.get(driverId);
    }
}
