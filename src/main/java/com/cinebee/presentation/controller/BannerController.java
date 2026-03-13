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

    /**
     * Táº¡o banner má»›i
     */
    @PostMapping("/add-banner")
    public ResponseEntity<BannerResponse> addBanner(@RequestBody BannerRequest request) {
        Banner banner = bannerService.createBanner(request);
        BannerResponse response = BannerMapper.toBannerResponse(banner);
        return ResponseEntity.ok(response);
    }

    /**
     * Update banner theo Movie ID - Tá»± Ä‘á»™ng táº¡o má»›i náº¿u chÆ°a cÃ³
     * Logic: 1 movie = 1 banner
     */
    @PostMapping("/update-banner/{movieId}")
    public ResponseEntity<BannerResponse> updateBannerByMovie(
            @PathVariable Long movieId, 
            @RequestBody BannerRequest request) {
        
        request.setMovieId(movieId);
        
        List<Banner> banners = bannerService.getBannersByMovieId(movieId);
        Banner result;
        
        if (banners.isEmpty()) {
            // Táº¡o banner má»›i náº¿u chÆ°a cÃ³
            result = bannerService.createBanner(request);
        } else {
            // Update banner Ä‘áº§u tiÃªn (should be only one)
            result = bannerService.updateBanner(banners.get(0).getId(), request);
        }
        
        BannerResponse response = BannerMapper.toBannerResponse(result);
        return ResponseEntity.ok(response);
    }
    
    /**
     * XÃ³a banner (soft delete)
     */
    @DeleteMapping("/delete-banner/{id}")
    public ResponseEntity<BannerResponse> deleteBanner(@PathVariable Long id) {
        Banner deletedBanner = bannerService.deleteBanner(id);
        BannerResponse response = BannerMapper.toBannerResponse(deletedBanner);
        return ResponseEntity.ok(response);
    }

    /**
     * Láº¥y táº¥t cáº£ banner Ä‘ang active
     */
    @GetMapping("/active")
    public ResponseEntity<List<BannerResponse>> getActiveBanners() {
        List<BannerResponse> responses = bannerService.getActiveBannerResponses();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Láº¥y táº¥t cáº£ banner (cho admin quáº£n lÃ½)
     */
    @GetMapping("/all")
    public ResponseEntity<List<BannerResponse>> getAllBanners() {
        List<BannerResponse> responses = bannerService.getAllBannerResponses();
        return ResponseEntity.ok(responses);
    }
    
    /**
     * Fix priority cho cÃ¡c banner cÅ© (maintenance)
     */
    @PostMapping("/fix-priorities")
    public ResponseEntity<String> fixNullPriorities() {
        bannerService.fixNullPriorities();
        return ResponseEntity.ok("âœ… ÄÃ£ fix priority cho cÃ¡c banner cÅ©");
    }
}

