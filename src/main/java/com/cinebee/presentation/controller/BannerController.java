package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.BannerRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.BannerResponse;
import com.cinebee.domain.entity.Banner;
import com.cinebee.application.mapper.BannerMapper;
import com.cinebee.application.service.BannerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/banner")
public class BannerController {

    @Autowired
    private BannerService bannerService;

    @PostMapping("/add-banner")
    public ResponseEntity<BaseResponse<BannerResponse>> addBanner(@RequestBody BannerRequest request) {
        Banner banner = bannerService.createBanner(request);
        BannerResponse response = BannerMapper.toBannerResponse(banner);
        return ResponseEntity.ok(BaseResponse.success(response, "Banner created successfully"));
    }

    @PostMapping("/update-banner/{movieId}")
    public ResponseEntity<BaseResponse<BannerResponse>> updateBannerByMovie(
            @PathVariable Long movieId, 
            @RequestBody BannerRequest request) {

        request.setMovieId(movieId);

        List<Banner> banners = bannerService.getBannersByMovieId(movieId);
        Banner result;

        if (banners.isEmpty()) {
            result = bannerService.createBanner(request);
        } else {
            result = bannerService.updateBanner(banners.get(0).getId(), request);
        }

        BannerResponse response = BannerMapper.toBannerResponse(result);
        return ResponseEntity.ok(BaseResponse.success(response, "Banner updated successfully"));
    }

    @DeleteMapping("/delete-banner/{id}")
    public ResponseEntity<BaseResponse<BannerResponse>> deleteBanner(@PathVariable Long id) {
        Banner deletedBanner = bannerService.deleteBanner(id);
        BannerResponse response = BannerMapper.toBannerResponse(deletedBanner);
        return ResponseEntity.ok(BaseResponse.success(response, "Banner deleted successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<BaseResponse<List<BannerResponse>>> getActiveBanners() {
        List<BannerResponse> responses = bannerService.getActiveBannerResponses();
        return ResponseEntity.ok(BaseResponse.success(responses, "Fetched active banners successfully"));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse<List<BannerResponse>>> getAllBanners() {
        List<BannerResponse> responses = bannerService.getAllBannerResponses();
        return ResponseEntity.ok(BaseResponse.success(responses, "Fetched banners successfully"));
    }

    @PostMapping("/fix-priorities")
    public ResponseEntity<BaseResponse<String>> fixNullPriorities() {
        bannerService.fixNullPriorities();
        return ResponseEntity.ok(BaseResponse.success("Banner priorities normalized", "Banner priorities fixed"));
    }
}

