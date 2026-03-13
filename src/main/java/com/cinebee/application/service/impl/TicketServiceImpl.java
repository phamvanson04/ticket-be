package com.cinebee.application.service.impl;

import com.cinebee.shared.common.Role;
import com.cinebee.presentation.dto.request.BookingRequest;
import com.cinebee.presentation.dto.response.BookingResponse;
import com.cinebee.domain.entity.Payment;
import com.cinebee.domain.entity.Seat;
import com.cinebee.domain.entity.Showtime;
import com.cinebee.domain.entity.Ticket;
import com.cinebee.domain.entity.User;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.PaymentRepository;
import com.cinebee.infrastructure.persistence.repository.SeatRepository;
import com.cinebee.infrastructure.persistence.repository.ShowtimeRepository;
import com.cinebee.infrastructure.persistence.repository.TicketRepository;
import com.cinebee.infrastructure.persistence.repository.UserRepository;
import com.cinebee.application.service.TicketService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

  private final TicketRepository ticketRepository;
  private final UserRepository userRepository;
  private final ShowtimeRepository showtimeRepository;
  private final SeatRepository seatRepository;
  private final PaymentRepository paymentRepository;

  @Override
  @Transactional
  public BookingResponse createBooking(BookingRequest request) {
    User currentUser = getCurrentUser();

    validateBookingRequest(request);

    Showtime showtime =
        showtimeRepository
            .findById(request.getShowtimeId())
            .orElseThrow(() -> new ApiException(ErrorCode.SHOWTIME_NOT_FOUND));

    if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
      throw new ApiException(ErrorCode.SHOWTIME_HAS_PASSED);
    }

    Set<String> normalizedSeatNumbers =
        request.getSeatNumbers().stream()
            .map(String::trim)
            .filter(StringUtils::hasText)
            .collect(Collectors.toCollection(LinkedHashSet::new));

    List<String> selectedSeatNumbers = new ArrayList<>(normalizedSeatNumbers);

    // Lock selected seats in DB to prevent race conditions on concurrent booking.
    List<Seat> lockedSeats =
        seatRepository.findByShowtimeIdAndSeatNumberInForUpdate(showtime.getId(), selectedSeatNumbers);

    if (lockedSeats.size() != selectedSeatNumbers.size()) {
      throw new ApiException(
          ErrorCode.SEAT_NOT_FOUND, "One or more selected seats do not exist for this showtime.");
    }

    for (Seat seat : lockedSeats) {
      if (!Boolean.TRUE.equals(seat.getIsAvailable())) {
        throw new ApiException(
            ErrorCode.SEAT_NOT_AVAILABLE, "Seat " + seat.getSeatNumber() + " is already booked.");
      }
    }

    String bookingReference = UUID.randomUUID().toString();
    List<Ticket> createdTickets = new ArrayList<>();
    double totalPrice = 0.0;

    for (Seat seat : lockedSeats) {
      seat.setIsAvailable(false);

      Ticket ticket = new Ticket();
      ticket.setUser(currentUser);
      ticket.setShowtime(showtime);
      ticket.setSeat(seat);
      ticket.setBookingReference(bookingReference);

      double ticketPrice = showtime.getMovie().getBasePrice() * seat.getPriceModifier();
      ticket.setPrice(ticketPrice);
      totalPrice += ticketPrice;

      createdTickets.add(ticket);
    }

    seatRepository.saveAll(lockedSeats);
    List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);

    log.info(
        "Successfully created {} tickets for user {} with bookingReference {}",
        savedTickets.size(),
        currentUser.getUsername(),
        bookingReference);

    return mapToBookingResponse(savedTickets.get(0), savedTickets, totalPrice);
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookingResponse> getUserBookings() {
    User currentUser = getCurrentUser();

    List<Ticket> tickets = ticketRepository.findByUserIdOrderByBookedAtDesc(currentUser.getId());
    if (tickets.isEmpty()) {
      return List.of();
    }

    Map<String, List<Ticket>> groupedByBookingRef =
        tickets.stream().collect(Collectors.groupingBy(this::resolveBookingGroupKey));

    return groupedByBookingRef.values().stream()
        .map(this::toBookingResponse)
        .sorted(Comparator.comparing(BookingResponse::getBookedAt).reversed())
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public BookingResponse getBookingDetails(Long ticketId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new ApiException(ErrorCode.TICKET_NOT_FOUND));

    User currentUser = getCurrentUser();
    enforceTicketOwnership(currentUser, ticket);

    List<Ticket> bookingTickets = resolveBookingTickets(ticket);
    return toBookingResponse(bookingTickets);
  }

  @Override
  @Transactional
  public void cancelBooking(Long ticketId) {
    Ticket ticket =
        ticketRepository
            .findById(ticketId)
            .orElseThrow(() -> new ApiException(ErrorCode.TICKET_NOT_FOUND));

    User currentUser = getCurrentUser();
    enforceTicketOwnership(currentUser, ticket);

    List<Ticket> bookingTickets = resolveBookingTickets(ticket);

    if (bookingTickets.stream().allMatch(value -> Boolean.TRUE.equals(value.getIsCancelled()))) {
      throw new ApiException(ErrorCode.BOOKING_ALREADY_CANCELLED);
    }

    boolean hasCompletedPayment =
        paymentRepository.existsByTicketInAndPaymentStatus(bookingTickets, Payment.PaymentStatus.COMPLETED);
    if (hasCompletedPayment) {
      throw new ApiException(ErrorCode.BOOKING_ALREADY_PAID);
    }

    LocalDateTime now = LocalDateTime.now();
    for (Ticket bookingTicket : bookingTickets) {
      if (Boolean.TRUE.equals(bookingTicket.getIsCancelled())) {
        continue;
      }

      if (bookingTicket.getBookedAt() != null && now.isAfter(bookingTicket.getBookedAt().plusHours(1))) {
        throw new ApiException(ErrorCode.CANCELLATION_WINDOW_EXPIRED);
      }

      if (bookingTicket.getShowtime().getStartTime().isBefore(now)) {
        throw new ApiException(ErrorCode.SHOWTIME_HAS_PASSED);
      }

      bookingTicket.setIsCancelled(true);
      bookingTicket.setCancelledAt(now);

      Seat seat = bookingTicket.getSeat();
      seat.setIsAvailable(true);
    }

    ticketRepository.saveAll(bookingTickets);
    seatRepository.saveAll(
        bookingTickets.stream().map(Ticket::getSeat).distinct().collect(Collectors.toList()));
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookingResponse.SeatInfo> getAvailableSeats(Long showtimeId) {
    if (!showtimeRepository.existsById(showtimeId)) {
      throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND);
    }

    List<Seat> allSeats = seatRepository.findByShowtimeIdAndIsAvailableTrue(showtimeId);

    return allSeats.stream()
        .map(
            seat -> {
              double finalPrice = seat.getShowtime().getMovie().getBasePrice() * seat.getPriceModifier();
              return new BookingResponse.SeatInfo(
                  seat.getSeatNumber(), seat.getSeatType().name(), finalPrice);
            })
        .collect(Collectors.toList());
  }

  private User getCurrentUser() {
    String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
    return userRepository
        .findByUsername(currentUsername)
        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));
  }

  private void validateBookingRequest(BookingRequest request) {
    if (request == null || request.getShowtimeId() == null) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "Showtime is required.");
    }

    if (CollectionUtils.isEmpty(request.getSeatNumbers())) {
      throw new ApiException(ErrorCode.BAD_REQUEST, "Seat list must not be empty.");
    }
  }

  private void enforceTicketOwnership(User currentUser, Ticket ticket) {
    if (currentUser.getRole() == Role.ADMIN) {
      return;
    }

    if (ticket.getUser() == null || !ticket.getUser().getId().equals(currentUser.getId())) {
      throw new ApiException(ErrorCode.UNAUTHORIZED);
    }
  }

  private List<Ticket> resolveBookingTickets(Ticket ticket) {
    if (StringUtils.hasText(ticket.getBookingReference())) {
      List<Ticket> grouped =
          ticketRepository.findByBookingReferenceOrderByBookedAtAsc(ticket.getBookingReference());
      if (!grouped.isEmpty()) {
        return grouped;
      }
    }

    return List.of(ticket);
  }

  private String resolveBookingGroupKey(Ticket ticket) {
    if (StringUtils.hasText(ticket.getBookingReference())) {
      return ticket.getBookingReference();
    }
    return "legacy-" + ticket.getId();
  }

  private BookingResponse toBookingResponse(List<Ticket> bookingTickets) {
    Ticket representative = bookingTickets.get(0);
    double totalPrice = bookingTickets.stream().mapToDouble(Ticket::getPrice).sum();
    return mapToBookingResponse(representative, bookingTickets, totalPrice);
  }

  private BookingResponse mapToBookingResponse(
      Ticket representativeTicket, List<Ticket> allTickets, double totalPrice) {
    BookingResponse response = new BookingResponse();
    response.setTicketId(representativeTicket.getId());
    response.setShowtimeId(representativeTicket.getShowtime().getId());
    response.setMovieTitle(representativeTicket.getShowtime().getMovie().getTitle());
    response.setTheaterName(representativeTicket.getShowtime().getTheater().getName());
    response.setRoomName(representativeTicket.getShowtime().getRoom().getName());
    response.setShowtime(representativeTicket.getShowtime().getStartTime());
    response.setBookedAt(representativeTicket.getBookedAt());
    response.setStatus(Boolean.TRUE.equals(representativeTicket.getIsCancelled()) ? "CANCELLED" : "PENDING_PAYMENT");
    response.setTotalPrice(totalPrice);

    List<BookingResponse.SeatInfo> seatInfos =
        allTickets.stream()
            .map(
                ticket ->
                    new BookingResponse.SeatInfo(
                        ticket.getSeat().getSeatNumber(),
                        ticket.getSeat().getSeatType().name(),
                        ticket.getPrice()))
            .collect(Collectors.toList());
    response.setSeats(seatInfos);

    return response;
  }
}

