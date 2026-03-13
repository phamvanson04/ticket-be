package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.ShowtimeRequest;
import com.cinebee.presentation.dto.response.ShowtimeResponse;
import com.cinebee.application.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/showtimes")
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    // Create new showtime (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowtimeResponse> createShowtime(@Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse createdShowtime = showtimeService.createShowtime(request);
        return new ResponseEntity<>(createdShowtime, HttpStatus.CREATED);
    }

    // Update showtime (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ShowtimeResponse> updateShowtime(@PathVariable Long id, @Valid @RequestBody ShowtimeRequest request) {
        ShowtimeResponse updatedShowtime = showtimeService.updateShowtime(id, request);
        return ResponseEntity.ok(updatedShowtime);
    }

    // Delete showtime (Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShowtime(@PathVariable Long id) {
        showtimeService.deleteShowtime(id);
        return ResponseEntity.noContent().build();
    }

    // Get all showtimes with pagination (Public)
    @GetMapping
    public ResponseEntity<Page<ShowtimeResponse>> getAllShowtimes(Pageable pageable) {
        Page<ShowtimeResponse> showtimes = showtimeService.getAllShowtimes(pageable);
        return ResponseEntity.ok(showtimes);
    }

    // Get showtime by ID (Public)
    @GetMapping("/{id}")
    public ResponseEntity<ShowtimeResponse> getShowtimeById(@PathVariable Long id) {
        ShowtimeResponse showtime = showtimeService.getShowtimeById(id);
        return ResponseEntity.ok(showtime);
    }

    // Get showtimes by movie ID (Public)
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovie(@PathVariable Long movieId) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByMovie(movieId);
        return ResponseEntity.ok(showtimes);
    }

    // Get showtimes by theater ID (Public)
    @GetMapping("/theater/{theaterId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByTheater(@PathVariable Long theaterId) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByTheater(theaterId);
        return ResponseEntity.ok(showtimes);
    }

    // Get showtimes by movie and theater (Public)
    @GetMapping("/movie/{movieId}/theater/{theaterId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByMovieAndTheater(
            @PathVariable Long movieId,
            @PathVariable Long theaterId) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByMovieAndTheater(movieId, theaterId);
        return ResponseEntity.ok(showtimes);
    }

    // Get showtimes by date (Public)
    @GetMapping("/date/{date}")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesByDate(date);
        return ResponseEntity.ok(showtimes);
    }

    // Get available seats for a showtime (Public)
    @GetMapping("/{id}/seats")
    public ResponseEntity<List<Integer>> getAvailableSeats(@PathVariable Long id) {
        List<Integer> availableSeats = showtimeService.getAvailableSeats(id);
        return ResponseEntity.ok(availableSeats);
    }

    // Get showtimes by movie, theater and date for booking (Public)
    @GetMapping("/booking")
    public ResponseEntity<List<ShowtimeResponse>> getShowtimesForBooking(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theaterId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowtimeResponse> showtimes = showtimeService.getShowtimesForBooking(movieId, theaterId, date);
        return ResponseEntity.ok(showtimes);
    }
}

