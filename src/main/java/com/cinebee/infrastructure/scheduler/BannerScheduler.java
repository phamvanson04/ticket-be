package com.cinebee.infrastructure.scheduler;

import com.cinebee.domain.entity.Banner;
import com.cinebee.infrastructure.persistence.repository.BannerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class BannerScheduler {
    @Autowired
    private BannerRepository bannerRepository;

    @Scheduled(fixedRate = 5000)
    public void updateBannerStatus() {
        LocalDate today = LocalDate.now();
        List<Banner> allBanners = bannerRepository.findAll();
        boolean hasChanges = false;

        for (Banner banner : allBanners) {
            boolean shouldBeActive = banner.getStartDate() != null 
                && banner.getEndDate() != null
                && !today.isBefore(banner.getStartDate())
                && !today.isAfter(banner.getEndDate());

            if (banner.isActive() != shouldBeActive) {
                banner.setActive(shouldBeActive);
                hasChanges = true;
                
                if (shouldBeActive) {
                    log.info("[BannerScheduler] Banner ID {} '{}' activated", 
                        banner.getId(), banner.getTitle());
                } else {
                    log.info("[BannerScheduler] Banner ID {} '{}' deactivated", 
                        banner.getId(), banner.getTitle());
                }
            }
        }

        if (hasChanges) {
            bannerRepository.saveAll(allBanners);
            log.info("[BannerScheduler] Banner statuses updated.");
        }
    }
}

