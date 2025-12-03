package com.carry_guide.carry_guide_admin.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerAnalyticsDTO {

    // new customers based on createdDate
    private long newCustomers7Days;
    private long newCustomersThisMonth;
    private long newCustomersThisYear;

    // total registered customers
    private long totalUniqueCustomers;
}