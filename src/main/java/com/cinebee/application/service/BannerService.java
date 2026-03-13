package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.BannerRequest;
import com.cinebee.domain.entity.Banner;

import java.util.List;

import com.cinebee.presentation.dto.response.BannerResponse;

public interface BannerService {

    Banner createBanner(BannerRequest request);

    List<Banner> getActiveBanners();
    
    // âœ¨ Method má»›i cache BannerResponse thay vÃ¬ Banner entity
    List<BannerResponse> getActiveBannerResponses();
    
    // âœ¨ Method Ä‘á»ƒ láº¥y táº¥t cáº£ banner (cho admin)
    List<BannerResponse> getAllBannerResponses();

    Banner updateBanner(Long id, BannerRequest request);

    Banner deleteBanner(Long id);
    
    // âœ¨ TÃ¬m banner theo movieId
    List<Banner> getBannersByMovieId(Long movieId);
    
    // âœ¨ Method Ä‘á»ƒ fix priority cho banner cÅ©
    void fixNullPriorities();
}

