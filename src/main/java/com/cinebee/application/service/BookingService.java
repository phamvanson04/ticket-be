package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.BookingRequest;
import com.cinebee.presentation.dto.response.BookingResponse;

import java.util.List;

public interface BookingService {

    /**
     * Create a booking for selected seats.
     * @param request booking input containing showtimeId and selected seats
     * @return booking summary
     */
    BookingResponse createBooking(BookingRequest request);

    /**
     * Get bookings of the current authenticated user.
     * @return list of booking summaries
     */
    List<BookingResponse> getUserBookings();

    /**
     * Get booking details for one ticket.
     * @param ticketId ticket identifier
     * @return booking details
     */
    BookingResponse getBookingDetails(Long ticketId);

    /**
     * Cancel a booking when payment is not completed.
     * @param ticketId ticket identifier
     */
    void cancelBooking(Long ticketId);

    /**
     * Get all currently available seats for a showtime.
     * @param showtimeId showtime identifier
     * @return available seat information
     */
    List<BookingResponse.SeatInfo> getAvailableSeats(Long showtimeId);
}

