package com.cinebee.presentation.dto.response;

import java.io.Serializable;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerResponse implements Serializable {
  private static final long serialVersionUID = 1L;

  private Long id;
  private String title;
  private String description;
  private String bannerUrl;
  private LocalDate startDate;
  private LocalDate endDate;
  private boolean active;
  private Long movieId;
  private Integer priority;
}

