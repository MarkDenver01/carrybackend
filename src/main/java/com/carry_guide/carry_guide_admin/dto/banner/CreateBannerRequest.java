package com.carry_guide.carry_guide_admin.dto.banner;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateBannerRequest {

    // Cloudinary URL (image)
    private String bannerUrl;

    // Redirect URL (where the user goes when tapping banner)
    private String bannerUrlLink;
}