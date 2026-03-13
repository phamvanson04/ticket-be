package com.cinebee.application.service;

import com.cinebee.domain.entity.Banner;
import com.cinebee.presentation.dto.request.BannerRequest;
import com.cinebee.presentation.dto.response.BannerResponse;
import java.util.List;

public interface BannerService {

    Banner createBanner(BannerRequest request);

    List<Banner> getActiveBanners();

    List<BannerResponse> getActiveBannerResponses();

    List<BannerResponse> getAllBannerResponses();

    Banner updateBanner(Long id, BannerRequest request);

    Banner deleteBanner(Long id);

    List<Banner> getBannersByMovieId(Long movieId);

    void fixNullPriorities();
}

