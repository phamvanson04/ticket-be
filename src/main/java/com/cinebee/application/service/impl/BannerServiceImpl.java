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

    @Override
    @CacheEvict(value = "activeBanners", allEntries = true)
    @Transactional
    public Banner createBanner(BannerRequest request) {
        if (request.getMovieId() != null) {
            List<Banner> existingBanners = bannerRepository.findByMovieId(request.getMovieId());
            if (!existingBanners.isEmpty()) {
                logger.warn("Movie {} already has {} banner(s). Updating the first one.", request.getMovieId(), existingBanners.size());

                Banner existingBanner = existingBanners.get(0);
                logger.info("Auto-updating existing banner {} instead of creating a new one", existingBanner.getId());
                return updateBanner(existingBanner.getId(), request);
            }
        }

        Banner banner = new Banner();
        mapRequestToBanner(banner, request);

        LocalDate today = LocalDate.now();
        boolean shouldBeActive = request.getStartDate() != null
            && request.getEndDate() != null
            && !today.isBefore(request.getStartDate())
            && !today.isAfter(request.getEndDate());

        banner.setActive(shouldBeActive);

        if (request.getMovieId() != null) {
            Movie movie = ServiceUtils.findObjectOrThrow(() -> movieRepository.findById(request.getMovieId()), ErrorCode.MOVIE_NOT_FOUND);
            banner.setMovie(movie);
        }

        if (shouldBeActive) {
            logger.info("Banner '{}' created and activated", banner.getTitle());
        } else {
            logger.info("Banner '{}' created but inactive due to date window", banner.getTitle());
        }

        return bannerRepository.save(banner);
    }


    @Override
    @CacheEvict(value = "activeBanners", allEntries = true)
    @Transactional
    public Banner updateBanner(Long id, BannerRequest request) {
        Banner banner = ServiceUtils.findObjectOrThrow(() -> bannerRepository.findById(id), ErrorCode.BANNER_NOT_FOUND);
        mapRequestToBanner(banner, request);

        LocalDate today = LocalDate.now();
        boolean shouldBeActive = request.getStartDate() != null
            && request.getEndDate() != null
            && !today.isBefore(request.getStartDate())
            && !today.isAfter(request.getEndDate());

        banner.setActive(shouldBeActive);

        if (shouldBeActive) {
            logger.info("Banner '{}' updated and activated", banner.getTitle());
        } else {
            logger.info("Banner '{}' updated but inactive due to date window", banner.getTitle());
        }

        return bannerRepository.save(banner);
    }

    private void mapRequestToBanner(Banner banner, BannerRequest request) {
        banner.setTitle(request.getTitle());
        banner.setDescription(request.getDescription());
        banner.setBannerUrl(request.getBannerUrl());
        banner.setStartDate(request.getStartDate());
        banner.setEndDate(request.getEndDate());
        
        if (request.getPriority() != null) {
            banner.setPriority(request.getPriority());
        } else {
            banner.setPriority((int) (System.currentTimeMillis() / 1000));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Banner> getActiveBanners() {
        LocalDate today = LocalDate.now();
        return bannerRepository.findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDescIdDesc(today, today);
    }

    @Override
    @Cacheable(value = "activeBanners", key = "'active-banner-responses'")
    @Transactional(readOnly = true)
    public List<BannerResponse> getActiveBannerResponses() {
        List<Banner> banners = getActiveBanners();
        return banners.stream()
                .map(BannerMapper::toBannerResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BannerResponse> getAllBannerResponses() {
        List<Banner> allBanners = bannerRepository.findAll();
        return allBanners.stream()
                .map(BannerMapper::toBannerResponse)
                .toList();
    }

    @Override
    @CacheEvict(value = "activeBanners", allEntries = true)
    @Transactional
    public Banner deleteBanner(Long id) {
        Banner banner = ServiceUtils.findObjectOrThrow(() -> bannerRepository.findById(id), ErrorCode.BANNER_NOT_FOUND);
        banner.setActive(false);
        return bannerRepository.save(banner);
    }

    @Override
    public List<Banner> getBannersByMovieId(Long movieId) {
        return bannerRepository.findByMovieId(movieId);
    }

    @Override
    @CacheEvict(value = "activeBanners", allEntries = true)
    @Transactional
    public void fixNullPriorities() {
        List<Banner> allBanners = bannerRepository.findAll();
        boolean hasChanges = false;

        for (Banner banner : allBanners) {
            if (banner.getPriority() == null) {
                banner.setPriority(banner.getId().intValue());
                hasChanges = true;
                logger.info("Fixed priority for banner ID {} to {}", banner.getId(), banner.getPriority());
            }
        }

        if (hasChanges) {
            bannerRepository.saveAll(allBanners);
            logger.info("Updated priority for {} banner(s)", allBanners.size());
        }
    }
}
