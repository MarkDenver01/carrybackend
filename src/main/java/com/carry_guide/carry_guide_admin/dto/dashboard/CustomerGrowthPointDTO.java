package com.carry_guide.carry_guide_admin.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerGrowthPointDTO {
    private String month;      // e.g. "Jan 2025"
    private long registered;   // customers created that month
    private long active;       // distinct customers with orders that month
}