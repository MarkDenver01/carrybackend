package com.carry_guide.carry_guide_admin.domain.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long adminId;
    private String userName;
    private String email;
    private String createdDate;
}
