package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.BannerRequest;
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
    public ResponseEntity<BannerResponse> addBanner(@RequestBody BannerRequest request) {
        Banner banner = bannerService.createBanner(request);
        BannerResponse response = BannerMapper.toBannerResponse(banner);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/update-banner/{movieId}")
    public ResponseEntity<BannerResponse> updateBannerByMovie(
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
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete-banner/{id}")
    public ResponseEntity<BannerResponse> deleteBanner(@PathVariable Long id) {
        Banner deletedBanner = bannerService.deleteBanner(id);
        BannerResponse response = BannerMapper.toBannerResponse(deletedBanner);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        List<BannerResponse> responses = bannerService.getActiveBannerResponses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/all")
    public ResponseEntity<List<BannerResponse>> getAllBanners() {
        List<BannerResponse> responses = bannerService.getAllBannerResponses();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/fix-priorities")
    public ResponseEntity<String> fixNullPriorities() {
        bannerService.fixNullPriorities();
        return ResponseEntity.ok("Banner priorities normalized");
    }
}

