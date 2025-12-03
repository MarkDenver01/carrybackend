package com.carry_guide.carry_guide_admin.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReturningVsNewDTO {
    private long newCustomers;        // exactly 1 order
    private long returningCustomers;  // 2+ orders
    private long totalCustomers;      // total in customer_info
}