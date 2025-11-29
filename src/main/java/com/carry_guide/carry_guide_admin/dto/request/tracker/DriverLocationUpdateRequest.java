package com.carry_guide.carry_guide_admin.dto.request.tracker;

import lombok.Data;

@Data
public class DriverLocationUpdateRequest {
    private String driverId;   // match your Rider.id or riderId as string
    private double latitude;
    private double longitude;
    private Float accuracy;    // optional
    private Long timestamp;    // epoch millis from mobile
}
