package com.cinebee.presentation.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShowtimeRequest {
    
    @NotNull(message = "Movie ID is required")
    private Long movieId;
    
    @NotNull(message = "Theater ID is required")
    private Long theaterId;
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;
    
    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
    
    @NotNull(message = "Price modifier is required")
    private Double priceModifier;
}

