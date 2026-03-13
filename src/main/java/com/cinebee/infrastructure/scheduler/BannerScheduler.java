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
    // fixedRate = 5000
    // Cháº¡y má»—i ngÃ y lÃºc 0h05 sÃ¡ng
    @Scheduled(fixedRate = 5000)
    public void updateBannerStatus() {
        LocalDate today = LocalDate.now();
        List<Banner> allBanners = bannerRepository.findAll();
        boolean hasChanges = false;

        for (Banner banner : allBanners) {
            boolean shouldBeActive = banner.getStartDate() != null 
                && banner.getEndDate() != null
                && !today.isBefore(banner.getStartDate())  // today >= startDate
                && !today.isAfter(banner.getEndDate());    // today <= endDate

            // Chá»‰ cáº­p nháº­t náº¿u tráº¡ng thÃ¡i thay Ä‘á»•i
            if (banner.isActive() != shouldBeActive) {
                banner.setActive(shouldBeActive);
                hasChanges = true;
                
                if (shouldBeActive) {
                    log.info("[BannerScheduler] âœ… Banner ID {} '{}' Ä‘Ã£ Ä‘Æ°á»£c KÃCH HOáº T (trong thá»i gian hiá»‡u lá»±c)", 
                        banner.getId(), banner.getTitle());
                } else {
                    log.info("[BannerScheduler] âŒ Banner ID {} '{}' Ä‘Ã£ bá»‹ VÃ” HIá»†U HÃ“A (háº¿t háº¡n hoáº·c chÆ°a Ä‘áº¿n ngÃ y)", 
                        banner.getId(), banner.getTitle());
                }
            }
        }

        if (hasChanges) {
            bannerRepository.saveAll(allBanners);
            log.info("[BannerScheduler] ðŸ”„ ÄÃ£ cáº­p nháº­t tráº¡ng thÃ¡i banner.");
        }
    }
}

