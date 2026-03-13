package com.cinebee.application.service.impl;

import com.cinebee.presentation.dto.request.BannerRequest;
import com.cinebee.presentation.dto.response.BannerResponse;
import com.cinebee.domain.entity.Banner;
import com.cinebee.domain.entity.Movie;
import com.cinebee.application.mapper.BannerMapper;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.BannerRepository;
import com.cinebee.infrastructure.persistence.repository.MovieRepository;
import com.cinebee.application.service.BannerService;
import com.cinebee.shared.util.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class BannerServiceImpl implements BannerService {
    private static final Logger logger = LoggerFactory.getLogger(BannerServiceImpl.class);
    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private MovieRepository movieRepository;

    // Táº¡o má»›i banner (vá»›i validation)
    @Override
    @CacheEvict(value = "activeBanners", allEntries = true) // Clear cache khi táº¡o banner má»›i
    @Transactional
    public Banner createBanner(BannerRequest request) {
        // âœ¨ VALIDATION: Kiá»ƒm tra xem phim Ä‘Ã£ cÃ³ banner chÆ°a
        if (request.getMovieId() != null) {
            List<Banner> existingBanners = bannerRepository.findByMovieId(request.getMovieId());
            if (!existingBanners.isEmpty()) {
                logger.warn("âš ï¸ Movie {} Ä‘Ã£ cÃ³ {} banner(s) tá»“n táº¡i!", request.getMovieId(), existingBanners.size());
                
                // Tá»° Äá»˜NG UPDATE BANNER Äáº¦U TIÃŠN THAY VÃŒ Táº O Má»šI
                Banner existingBanner = existingBanners.get(0);
                logger.info("ðŸ”„ Tá»± Ä‘á»™ng cáº­p nháº­t banner {} thay vÃ¬ táº¡o má»›i", existingBanner.getId());
                return updateBanner(existingBanner.getId(), request);
            }
        }
        
        Banner banner = new Banner();
        mapRequestToBanner(banner, request);
        
  // Tá»± Ä‘á»™ng set isActive dá»±a trÃªn ngÃ y
        LocalDate today = LocalDate.now();
        boolean shouldBeActive = request.getStartDate() != null 
            && request.getEndDate() != null
            && !today.isBefore(request.getStartDate())  // today >= startDate
            && !today.isAfter(request.getEndDate());    // today <= endDate
            
        banner.setActive(shouldBeActive);
        
        if (request.getMovieId() != null) {
            Movie movie = ServiceUtils.findObjectOrThrow(() -> movieRepository.findById(request.getMovieId()), ErrorCode.MOVIE_NOT_FOUND);
            banner.setMovie(movie);
        }
        
        if (shouldBeActive) {
            logger.info("âœ… Banner '{}' Ä‘Æ°á»£c táº¡o vÃ  KÃCH HOáº T (trong thá»i gian hiá»‡u lá»±c)", banner.getTitle());
        } else {
            logger.info("âŒ Banner '{}' Ä‘Æ°á»£c táº¡o nhÆ°ng VÃ” HIá»†U HÃ“A (ngoÃ i thá»i gian hiá»‡u lá»±c)", banner.getTitle());
        }
        
        return bannerRepository.save(banner);
    }


    // Cáº­p nháº­t thÃ´ng tin banner
    @Override
    @CacheEvict(value = "activeBanners", allEntries = true) // Clear cache khi update banner
    @Transactional
    public Banner updateBanner(Long id, BannerRequest request) {
        Banner banner = ServiceUtils.findObjectOrThrow(() -> bannerRepository.findById(id), ErrorCode.BANNER_NOT_FOUND);
        mapRequestToBanner(banner, request);
        
        // âœ¨ LOGIC Má»šI: Tá»± Ä‘á»™ng set isActive dá»±a trÃªn ngÃ y
        LocalDate today = LocalDate.now();
        boolean shouldBeActive = request.getStartDate() != null 
            && request.getEndDate() != null
            && !today.isBefore(request.getStartDate())  // today >= startDate
            && !today.isAfter(request.getEndDate());    // today <= endDate
            
        banner.setActive(shouldBeActive);
        
        if (shouldBeActive) {
            logger.info("âœ… Banner '{}' Ä‘Æ°á»£c cáº­p nháº­t vÃ  KÃCH HOáº T (trong thá»i gian hiá»‡u lá»±c)", banner.getTitle());
        } else {
            logger.info("âŒ Banner '{}' Ä‘Æ°á»£c cáº­p nháº­t nhÆ°ng VÃ” HIá»†U HÃ“A (ngoÃ i thá»i gian hiá»‡u lá»±c)", banner.getTitle());
        }
        
        return bannerRepository.save(banner);
    }

    private void mapRequestToBanner(Banner banner, BannerRequest request) {
        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setBannerUrl(request.getBannerUrl());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        
        // Set priority - náº¿u khÃ´ng cÃ³ thÃ¬ dÃ¹ng giÃ¡ trá»‹ cao (banner má»›i Æ°u tiÃªn)
        if (request.getPriority() != null) {
            banner.setPriority(request.getPriority());
        } else {
            // Banner má»›i khÃ´ng cÃ³ priority thÃ¬ set = timestamp Ä‘á»ƒ luÃ´n lÃªn Ä‘áº§u
            banner.setPriority((int) (System.currentTimeMillis() / 1000));
        }
    }

    // Láº¥y danh sÃ¡ch banner cÃ²n hiá»‡u lá»±c
    @Override
    // @Cacheable(value = "activeBanners", key = "'active-banners-' + #root.methodName") // Táº¯t cache vÃ¬ serialization issue
    @Transactional(readOnly = true)
    public List<Banner> getActiveBanners() {
        LocalDate today = LocalDate.now();
        return bannerRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDescIdDesc(today, today);
    }
    
    // âœ¨ Method má»›i cache BannerResponse (cÃ³ thá»ƒ serialize)
    @Override
    @Cacheable(value = "activeBanners", key = "'active-banner-responses'")
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBannerResponses() {
        List<Banner> banners = getActiveBanners();
        return banners.stream()
                .map(BannerMapper::toBannerResponse)
                .toList();
    }
    
    // âœ¨ Method Ä‘á»ƒ láº¥y táº¥t cáº£ banner (cho admin)
    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBannerResponses() {
        List<Banner> allBanners = bannerRepository.findAll();
        return allBanners.stream()
                .map(BannerMapper::toBannerResponse)
                .toList();
    }
    // XÃ³a banner báº±ng cÃ¡ch Ä‘Ã¡nh dáº¥u lÃ  khÃ´ng hoáº¡t Ä‘á»™ng
    @Override
    @CacheEvict(value = "activeBanners", allEntries = true) // Clear cache khi delete banner
    @Transactional
    public Banner deleteBanner(Long id) {
        Banner banner = ServiceUtils.findObjectOrThrow(() -> bannerRepository.findById(id), ErrorCode.BANNER_NOT_FOUND);
        banner.setActive(false);
        return bannerRepository.save(banner);
    }
    
    // âœ¨ TÃ¬m banner theo movieId
    @Override
    public List<Banner> getBannersByMovieId(Long movieId) {
        return bannerRepository.findByMovieId(movieId);
    }
    
    // âœ¨ Method Ä‘á»ƒ fix priority cho banner cÅ©
    @Override
    @CacheEvict(value = "activeBanners", allEntries = true) // Clear cache sau khi fix
    @Transactional
    public void fixNullPriorities() {
        List<Banner> allBanners = bannerRepository.findAll();
        boolean hasChanges = false;
        
        for (Banner banner : allBanners) {
            if (banner.getPriority() == null) {
                // Set priority = ID Ä‘á»ƒ banner cÅ© cÃ³ thá»© tá»± theo ID
                banner.setPriority(banner.getId().intValue());
                hasChanges = true;
                logger.info("ðŸ”§ Fixed priority for banner ID {} to {}", banner.getId(), banner.getPriority());
            }
        }
        
        if (hasChanges) {
            bannerRepository.saveAll(allBanners);
            logger.info("âœ… ÄÃ£ cáº­p nháº­t priority cho {} banner", allBanners.size());
        }
    }
}
