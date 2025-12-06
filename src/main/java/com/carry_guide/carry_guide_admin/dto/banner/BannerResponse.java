package com.carry_guide.carry_guide_admin.dto.banner;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BannerResponse {

    private Long bannerId;

    private String bannerUrl;

    private String bannerUrlLink;

    private LocalDateTime createdAt;
}