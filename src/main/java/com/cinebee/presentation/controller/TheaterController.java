package com.cinebee.presentation.controller;

import com.cinebee.presentation.dto.request.TheaterRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.TheaterResponse;
import com.cinebee.application.service.TheaterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    // Endpoint for creating a new theater (Admin only)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TheaterResponse>> createTheater(@Valid @RequestBody TheaterRequest request) {
        TheaterResponse createdTheater = theaterService.createTheater(request);
        return new ResponseEntity<>(BaseResponse.success(createdTheater, "Theater created successfully"), HttpStatus.CREATED);
    }

    // Endpoint for updating an existing theater (Admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<TheaterResponse>> updateTheater(@PathVariable Long id, @Valid @RequestBody TheaterRequest request) {
        TheaterResponse updatedTheater = theaterService.updateTheater(id, request);
        return ResponseEntity.ok(BaseResponse.success(updatedTheater, "Theater updated successfully"));
    }

    // Endpoint for "deleting" a theater (soft delete, Admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaseResponse<Void>> deleteTheater(@PathVariable Long id) {
        theaterService.deleteTheater(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Theater deleted successfully"));
    }

    // Endpoint to get a list of all theaters with pagination (Public)
    @GetMapping
    public ResponseEntity<BaseResponse<Page<TheaterResponse>>> getAllTheaters(Pageable pageable) {
        Page<TheaterResponse> theaters = theaterService.getAllTheaters(pageable);
        return ResponseEntity.ok(BaseResponse.success(theaters, "Fetched theaters successfully"));
    }

    // Endpoint to get a single theater by its ID (Public)
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<TheaterResponse>> getTheaterById(@PathVariable Long id) {
        TheaterResponse theater = theaterService.getTheaterById(id);
        return ResponseEntity.ok(BaseResponse.success(theater, "Fetched theater successfully"));
    }
}

