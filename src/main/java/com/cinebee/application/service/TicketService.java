package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.BookingRequest;
import com.cinebee.presentation.dto.response.BookingResponse;

import java.util.List;

public interface TicketService {
    
    /**
     * Táº¡o booking cho gháº¿ Ä‘Ã£ chá»n
     * @param request BookingRequest chá»©a showtimeId vÃ  danh sÃ¡ch gháº¿
     * @return BookingResponse vá»›i thÃ´ng tin vÃ© Ä‘Ã£ táº¡o
     */
    BookingResponse createBooking(BookingRequest request);
    
    /**
     * Láº¥y danh sÃ¡ch vÃ© cá»§a user hiá»‡n táº¡i
     * @return Danh sÃ¡ch BookingResponse
     */
    List<BookingResponse> getUserBookings();
    
    /**
     * Láº¥y thÃ´ng tin chi tiáº¿t 1 vÃ©
     * @param ticketId ID cá»§a vÃ©
     * @return BookingResponse
     */
    BookingResponse getBookingDetails(Long ticketId);
    
    /**
     * Há»§y vÃ© (chá»‰ khi chÆ°a thanh toÃ¡n)
     * @param ticketId ID cá»§a vÃ© cáº§n há»§y
     */
    void cancelBooking(Long ticketId);
    
    /**
     * Láº¥y danh sÃ¡ch gháº¿ cÃ³ sáºµn cho 1 suáº¥t chiáº¿u
     * @param showtimeId ID cá»§a suáº¥t chiáº¿u
     * @return Danh sÃ¡ch thÃ´ng tin gháº¿
     */
    List<BookingResponse.SeatInfo> getAvailableSeats(Long showtimeId);
}

