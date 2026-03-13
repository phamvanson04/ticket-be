package com.cinebee.presentation.dto.request;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
@Getter
@Setter
public class BannerRequest {
    private String title;
    private String description;
    private String bannerUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long movieId;
    private Integer priority;

}
