package com.cinebee.infrastructure.scheduler;

import com.cinebee.application.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CacheWarmer implements ApplicationRunner {

    @Autowired
    private BannerService bannerService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Application started. Warming up active banners cache...");
        try {
            bannerService.getActiveBannerResponses();
            log.info("Active banners cache has been successfully warmed up.");
        } catch (Exception e) {
            log.error("Error warming up active banners cache", e);
        }
    }
}

