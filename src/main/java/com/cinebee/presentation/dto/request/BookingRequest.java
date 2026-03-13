package com.cinebee.presentation.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class BookingRequest {
    private Long showtimeId;
    private List<String> seatNumbers;

    public BookingRequest(Long showtimeId, List<String> seatNumbers) {
        this.showtimeId = showtimeId;
        this.seatNumbers = seatNumbers;
    }
}

