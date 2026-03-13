package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.BookingRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.BookingResponse;
import com.cinebee.application.service.BookingService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

  private final BookingService bookingService;

  /** Creates a booking for selected seats. */
  @PostMapping("/book")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<BookingResponse>> createBooking(@RequestBody BookingRequest request) {
    log.info("Booking request received: {}", request);
    BookingResponse response = bookingService.createBooking(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.success(response, "Booking created successfully"));
  }

  /** Returns bookings of the current user. */
  @GetMapping("/my-bookings")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<List<BookingResponse>>> getUserBookings() {
    List<BookingResponse> bookings = bookingService.getUserBookings();
    return ResponseEntity.ok(BaseResponse.success(bookings, "Fetched bookings successfully"));
  }

  /** Returns booking details by ticket id. */
  @GetMapping("/{ticketId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<BookingResponse>> getBookingDetails(@PathVariable Long ticketId) {
    BookingResponse booking = bookingService.getBookingDetails(ticketId);
    return ResponseEntity.ok(BaseResponse.success(booking, "Fetched booking details successfully"));
  }

  /** Cancels a booking when payment has not completed. */
  @DeleteMapping("/{ticketId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BaseResponse<Void>> cancelBooking(@PathVariable Long ticketId) {
    bookingService.cancelBooking(ticketId);
    return ResponseEntity.ok(BaseResponse.success(null, "Booking cancelled successfully"));
  }

  /** Returns available seats for one showtime. */
  @GetMapping("/showtimes/{showtimeId}/seats")
  public ResponseEntity<BaseResponse<List<BookingResponse.SeatInfo>>> getAvailableSeats(@PathVariable Long showtimeId) {
    List<BookingResponse.SeatInfo> seats = bookingService.getAvailableSeats(showtimeId);
    return ResponseEntity.ok(BaseResponse.success(seats, "Fetched available seats successfully"));
  }
}

