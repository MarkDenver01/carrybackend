package com.carry_guide.carry_guide_admin.dto.membership;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberRowDTO {
    private String name;
    private String start;
    private String expiry;
    private Integer points;
    private String status;
}