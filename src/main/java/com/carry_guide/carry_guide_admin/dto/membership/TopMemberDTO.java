package com.carry_guide.carry_guide_admin.dto.membership;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopMemberDTO {
    private String name;
    private Integer points;
    private String expiry;
}