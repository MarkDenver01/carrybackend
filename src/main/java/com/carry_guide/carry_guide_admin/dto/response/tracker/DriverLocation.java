package com.carry_guide.carry_guide_admin.dto.response.tracker;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DriverLocation {
    private String driverId;
    private double latitude;
    private double longitude;
    private Float accuracy;
    private Long timestamp;
}
