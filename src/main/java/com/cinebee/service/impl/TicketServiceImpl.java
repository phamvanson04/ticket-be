package com.cinebee.service.impl;

import com.cinebee.dto.request.BookingRequest;
import com.cinebee.dto.response.BookingResponse;
import com.cinebee.entity.Seat;
import com.cinebee.entity.Showtime;
import com.cinebee.entity.Ticket;
import com.cinebee.entity.User;
import com.cinebee.exception.ApiException;
import com.cinebee.exception.ErrorCode;
import com.cinebee.repository.SeatRepository;
import com.cinebee.repository.ShowtimeRepository;
import com.cinebee.repository.TicketRepository;
import com.cinebee.repository.UserRepository;
import com.cinebee.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + currentUsername));

        validateBookingRequest(request);

        Showtime showtime = showtimeRepository.findById(request.getShowtimeId())
                .orElseThrow(() -> new ApiException(ErrorCode.SHOWTIME_NOT_FOUND));

        if (showtime.getStartTime().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.SHOWTIME_HAS_PASSED);
        }

        Set<String> normalizedSeatNumbers = request.getSeatNumbers().stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<String> selectedSeatNumbers = new ArrayList<>(normalizedSeatNumbers);

        // Lock selected seats in DB to prevent race condition when two users book same seats concurrently.
        List<Seat> lockedSeats = seatRepository.findByShowtimeIdAndSeatNumberInForUpdate(
                showtime.getId(),
                selectedSeatNumbers
        );

        if (lockedSeats.size() != selectedSeatNumbers.size()) {
            throw new ApiException(ErrorCode.SEAT_NOT_FOUND, "One or more selected seats do not exist for this showtime.");
        }

        for (Seat seat : lockedSeats) {
            if (!Boolean.TRUE.equals(seat.getIsAvailable())) {
                throw new ApiException(
                        ErrorCode.SEAT_NOT_AVAILABLE,
                        "Seat " + seat.getSeatNumber() + " is already booked."
                );
            }
        }

        List<Ticket> createdTickets = new ArrayList<>();
        double totalPrice = 0.0;
        String bookingReference = UUID.randomUUID().toString();

        for (Seat seat : lockedSeats) {
            seat.setIsAvailable(false);

            Ticket ticket = new Ticket();
            ticket.setUser(currentUser);
            ticket.setShowtime(showtime);
            ticket.setSeat(seat);
            ticket.setBookingReference(bookingReference);

            Double ticketPrice = showtime.getMovie().getBasePrice() * seat.getPriceModifier();
            ticket.setPrice(ticketPrice);
            totalPrice += ticketPrice;

            createdTickets.add(ticket);
        }

        seatRepository.saveAll(lockedSeats);
        List<Ticket> savedTickets = ticketRepository.saveAll(createdTickets);

        log.info("Successfully created {} tickets for user {} with bookingReference {}",
                savedTickets.size(), currentUsername, bookingReference);

        return mapToBookingResponse(savedTickets.get(0), savedTickets, totalPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponse.SeatInfo> getAvailableSeats(Long showtimeId) {
        if (!showtimeRepository.existsById(showtimeId)) {
            throw new ApiException(ErrorCode.SHOWTIME_NOT_FOUND);
        }

        List<Seat> allSeats = seatRepository.findByShowtimeIdAndIsAvailableTrue(showtimeId);

        return allSeats.stream()
                .map(seat -> {
                    Double finalPrice = seat.getShowtime().getMovie().getBasePrice() * seat.getPriceModifier();
                    return new BookingResponse.SeatInfo(seat.getSeatNumber(), seat.getSeatType().name(), finalPrice);
                })
                .collect(Collectors.toList());
    }

    private void validateBookingRequest(BookingRequest request) {
        if (request == null || request.getShowtimeId() == null) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Showtime is required.");
        }

        if (CollectionUtils.isEmpty(request.getSeatNumbers())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "Seat list must not be empty.");
        }
    }

    private BookingResponse mapToBookingResponse(Ticket representativeTicket, List<Ticket> allTickets, double totalPrice) {
        BookingResponse response = new BookingResponse();
        response.setTicketId(representativeTicket.getId());
        response.setShowtimeId(representativeTicket.getShowtime().getId());
        response.setMovieTitle(representativeTicket.getShowtime().getMovie().getTitle());
        response.setTheaterName(representativeTicket.getShowtime().getTheater().getName());
        response.setRoomName(representativeTicket.getShowtime().getRoom().getName());
        response.setShowtime(representativeTicket.getShowtime().getStartTime());
        response.setBookedAt(representativeTicket.getBookedAt());
        response.setStatus("PENDING_PAYMENT");
        response.setTotalPrice(totalPrice);

        List<BookingResponse.SeatInfo> seatInfos = allTickets.stream()
                .map(ticket -> new BookingResponse.SeatInfo(
                        ticket.getSeat().getSeatNumber(),
                        ticket.getSeat().getSeatType().name(),
                        ticket.getPrice()))
                .collect(Collectors.toList());
        response.setSeats(seatInfos);

        return response;
    }

    @Override
    public List<BookingResponse> getUserBookings() {
        log.warn("getUserBookings is not fully implemented yet.");
        return new ArrayList<>();
    }

    @Override
    public BookingResponse getBookingDetails(Long ticketId) {
        log.warn("getBookingDetails is not fully implemented yet.");
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ApiException(ErrorCode.TICKET_NOT_FOUND));
        return mapToBookingResponse(ticket, List.of(ticket), ticket.getPrice());
    }

    @Override
    @Transactional
    public void cancelBooking(Long ticketId) {
        log.warn("cancelBooking is not fully implemented yet.");
    }
}
