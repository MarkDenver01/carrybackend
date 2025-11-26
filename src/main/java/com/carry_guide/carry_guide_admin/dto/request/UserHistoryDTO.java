package com.carry_guide.carry_guide_admin.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserHistoryDTO {
    private Long customerId;
    private String productKeyword;
    private String dateTime;
}
