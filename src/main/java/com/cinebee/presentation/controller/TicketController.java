package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.BookingRequest;
import com.cinebee.presentation.dto.response.BookingResponse;
import com.cinebee.application.service.TicketService;
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
public class TicketController {

  private final TicketService ticketService;

  /** Creates a booking for selected seats. */
  @PostMapping("/book")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
    log.info("Booking request received: {}", request);
    BookingResponse response = ticketService.createBooking(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  /** Returns bookings of the current user. */
  @GetMapping("/my-bookings")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<List<BookingResponse>> getUserBookings() {
    List<BookingResponse> bookings = ticketService.getUserBookings();
    return ResponseEntity.ok(bookings);
  }

  /** Returns booking details by ticket id. */
  @GetMapping("/{ticketId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<BookingResponse> getBookingDetails(@PathVariable Long ticketId) {
    BookingResponse booking = ticketService.getBookingDetails(ticketId);
    return ResponseEntity.ok(booking);
  }

  /** Cancels a booking when payment has not completed. */
  @DeleteMapping("/{ticketId}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  public ResponseEntity<Void> cancelBooking(@PathVariable Long ticketId) {
    ticketService.cancelBooking(ticketId);
    return ResponseEntity.noContent().build();
  }

  /** Returns available seats for one showtime. */
  @GetMapping("/showtimes/{showtimeId}/seats")
  public ResponseEntity<List<BookingResponse.SeatInfo>> getAvailableSeats(@PathVariable Long showtimeId) {
    List<BookingResponse.SeatInfo> seats = ticketService.getAvailableSeats(showtimeId);
    return ResponseEntity.ok(seats);
  }
}

