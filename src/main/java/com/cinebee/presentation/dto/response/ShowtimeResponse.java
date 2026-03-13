package com.cinebee.presentation.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShowtimeResponse {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String moviePoster;
    private Long theaterId;
    private String theaterName;
    private String theaterLocation;
    private Long roomId;
    private String roomName;
    private String roomType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double priceModifier;
    private Double basePrice;
    private Double finalPrice;
    private Integer availableSeats;
    private Integer totalSeats;
    private LocalDateTime createdAt;
}

