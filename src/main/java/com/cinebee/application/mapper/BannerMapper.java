package com.cinebee.application.mapper;

import com.cinebee.presentation.dto.response.BannerResponse;
import com.cinebee.domain.entity.Banner;

public class BannerMapper {

    public static BannerResponse toBannerResponse(Banner banner) {
        return BannerResponse.builder()
                .id(banner.getId())
                .title(banner.getTitle())
                .description(banner.getDescription())
                .bannerUrl(banner.getBannerUrl())
                .startDate(banner.getStartDate())
                .endDate(banner.getEndDate())
                .active(banner.isActive())
                .movieId(banner.getMovie() != null ? banner.getMovie().getId() : null)
                .priority(banner.getPriority()) // ThÃªm priority
                .build();
    }
}
