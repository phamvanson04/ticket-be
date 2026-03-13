package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.ShowtimeRequest;
import com.cinebee.presentation.dto.response.ShowtimeResponse;
import com.cinebee.domain.entity.*;
import com.cinebee.shared.exception.ResourceNotFoundException;
import com.cinebee.infrastructure.persistence.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final TheaterRepository theaterRepository;
    private final RoomRepository roomRepository;
    private final TicketRepository ticketRepository;

    public ShowtimeResponse createShowtime(ShowtimeRequest request) {
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));
        
        Theater theater = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + request.getTheaterId()));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        Showtime showtime = new Showtime();
        showtime.setMovie(movie);
        showtime.setTheater(theater);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        // This line will cause an error if Showtime entity is not reverted
        // showtime.setPriceModifier(request.getPriceModifier()); 
        showtime.setCreatedAt(LocalDateTime.now());

        Showtime savedShowtime = showtimeRepository.save(showtime);
        return mapToResponse(savedShowtime);
    }

    public ShowtimeResponse updateShowtime(Long id, ShowtimeRequest request) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + request.getMovieId()));
        
        Theater theater = theaterRepository.findById(request.getTheaterId())
                .orElseThrow(() -> new ResourceNotFoundException("Theater not found with id: " + request.getTheaterId()));
        
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));

        showtime.setMovie(movie);
        showtime.setTheater(theater);
        showtime.setRoom(room);
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        // This line will cause an error if Showtime entity is not reverted
        // showtime.setPriceModifier(request.getPriceModifier());

        Showtime updatedShowtime = showtimeRepository.save(showtime);
        return mapToResponse(updatedShowtime);
    }

    public void deleteShowtime(Long id) {
        if (!showtimeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Showtime not found with id: " + id);
        }
        showtimeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<ShowtimeResponse> getAllShowtimes(Pageable pageable) {
        Page<Showtime> showtimes = showtimeRepository.findAll(pageable);
        return showtimes.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public ShowtimeResponse getShowtimeById(Long id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + id));
        return mapToResponse(showtime);
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovie(Long movieId) {
        List<Showtime> showtimes = showtimeRepository.findByMovieIdAndStartTimeAfter(movieId, LocalDateTime.now());
        return showtimes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByTheater(Long theaterId) {
        List<Showtime> showtimes = showtimeRepository.findByTheaterIdAndStartTimeAfter(theaterId, LocalDateTime.now());
        return showtimes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByMovieAndTheater(Long movieId, Long theaterId) {
        List<Showtime> showtimes = showtimeRepository.findByMovieIdAndTheaterIdAndStartTimeAfter(
                movieId, theaterId, LocalDateTime.now());
        return showtimes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesByDate(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<Showtime> showtimes = showtimeRepository.findByStartTimeBetween(startOfDay, endOfDay);
        return showtimes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Integer> getAvailableSeats(Long showtimeId) {
        Showtime showtime = showtimeRepository.findById(showtimeId)
                .orElseThrow(() -> new ResourceNotFoundException("Showtime not found with id: " + showtimeId));
        
        List<String> bookedSeatLabels = ticketRepository.findBookedSeatsByShowtime(showtimeId);
        
        Room room = showtime.getRoom();
        int totalSeats = room.getCapacity();
        
        List<Integer> availableSeatNumbers = new java.util.ArrayList<>();
        for (int i = 1; i <= totalSeats; i++) {
            String currentSeatLabel = convertToSeatLabel(i);
            if (!bookedSeatLabels.contains(currentSeatLabel)) {
                availableSeatNumbers.add(i);
            }
        }
        
        return availableSeatNumbers;
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> getShowtimesForBooking(Long movieId, Long theaterId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = (date != null) ? date.atStartOfDay() : now;
        LocalDateTime endTime = (date != null) ? date.atTime(23, 59, 59) : now.plusDays(7);

        List<Showtime> showtimes;
        
        if (movieId != null && theaterId != null) {
            showtimes = showtimeRepository.findByMovieIdAndTheaterIdAndStartTimeBetween(
                    movieId, theaterId, startTime, endTime);
        } else if (movieId != null) {
            showtimes = showtimeRepository.findByMovieIdAndStartTimeBetween(movieId, startTime, endTime);
        } else if (theaterId != null) {
            showtimes = showtimeRepository.findByTheaterIdAndStartTimeBetween(theaterId, startTime, endTime);
        } else {
            showtimes = showtimeRepository.findByStartTimeBetween(startTime, endTime);
        }
        
        return showtimes.stream()
                .filter(s -> s.getStartTime().isAfter(now))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ShowtimeResponse mapToResponse(Showtime showtime) {
        ShowtimeResponse response = new ShowtimeResponse();
        response.setId(showtime.getId());
        response.setMovieId(showtime.getMovie().getId());
        response.setMovieTitle(showtime.getMovie().getTitle());
        response.setMoviePoster(showtime.getMovie().getPosterUrl());
        response.setTheaterId(showtime.getTheater().getId());
        response.setTheaterName(showtime.getTheater().getName());
        response.setTheaterLocation(showtime.getTheater().getAddress());
        response.setRoomId(showtime.getRoom().getId());
        response.setRoomName(showtime.getRoom().getName());
        response.setRoomType(showtime.getRoom().getType());
        response.setStartTime(showtime.getStartTime());
        response.setEndTime(showtime.getEndTime());
        // response.setPriceModifier(showtime.getPriceModifier());
        
        Double basePrice = showtime.getMovie().getBasePrice();
        // Double finalPrice = basePrice * showtime.getPriceModifier();
        response.setBasePrice(basePrice);
        // response.setFinalPrice(finalPrice);
        
        List<String> bookedSeats = ticketRepository.findBookedSeatsByShowtime(showtime.getId());
        int totalSeats = showtime.getRoom().getCapacity();
        int availableSeats = totalSeats - bookedSeats.size();
        response.setAvailableSeats(availableSeats);
        response.setTotalSeats(totalSeats);
        
        response.setCreatedAt(showtime.getCreatedAt());
        return response;
    }
    
    private String convertToSeatLabel(int seatNumber) {
        int row = (seatNumber - 1) / 10;
        int col = (seatNumber - 1) % 10 + 1;
        return String.valueOf((char)('A' + row)) + col;
    }
}

