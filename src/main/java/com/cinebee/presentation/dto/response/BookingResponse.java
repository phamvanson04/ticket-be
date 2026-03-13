package com.cinebee.presentation.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class BookingResponse {
    private Long ticketId;
    private Long showtimeId;
    private String movieTitle;
    private String theaterName;
    private String roomName;
    private LocalDateTime showtime;
    private List<SeatInfo> seats;
    private Double totalPrice;
    private LocalDateTime bookedAt;
    private String status; // PENDING_PAYMENT, PAID, CANCELLED
    
    @Data
    @NoArgsConstructor
    public static class SeatInfo {
        private String seatNumber;
        private String seatType;
        private Double price;
        
        public SeatInfo(String seatNumber, String seatType, Double price) {
            this.seatNumber = seatNumber;
            this.seatType = seatType;
            this.price = price;
        }
    }
}

