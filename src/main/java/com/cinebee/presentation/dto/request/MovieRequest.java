package com.cinebee.presentation.dto.request;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MovieRequest {
    private String title;
    private String othernames;
    private Double basePrice;
    private Integer duration;
    private String genre;
    private String posterUrl;
    private String description;
    private String actors;
    private String director;
    private String country;
    private Date releaseDate;
    private Double discountPercentage;
    private String trailerUrl;
}

