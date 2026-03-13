package com.cinebee.application.service.impl;

import com.cinebee.presentation.dto.request.TheaterRequest;
import com.cinebee.presentation.dto.response.TheaterResponse;
import com.cinebee.domain.entity.Theater;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.infrastructure.persistence.repository.TheaterRepository;
import com.cinebee.application.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;

    @Override
    @Transactional
    public TheaterResponse createTheater(TheaterRequest request) {
        Theater theater = new Theater();
        mapRequestToEntity(request, theater);
        Theater savedTheater = theaterRepository.save(theater);
        return TheaterResponse.fromEntity(savedTheater);
    }

    @Override
    @Transactional
    public TheaterResponse updateTheater(Long id, TheaterRequest request) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.THEATER_NOT_FOUND));
        mapRequestToEntity(request, theater);
        Theater updatedTheater = theaterRepository.save(theater);
        return TheaterResponse.fromEntity(updatedTheater);
    }

    @Override
    @Transactional
    public void deleteTheater(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.THEATER_NOT_FOUND));
        // Soft delete by changing the status
        theater.setStatus(Theater.TheaterStatus.INACTIVE);
        theaterRepository.save(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public TheaterResponse getTheaterById(Long id) {
        Theater theater = theaterRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.THEATER_NOT_FOUND));
        return TheaterResponse.fromEntity(theater);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TheaterResponse> getAllTheaters(Pageable pageable) {
        return theaterRepository.findAll(pageable)
                .map(TheaterResponse::fromEntity);
    }

    private void mapRequestToEntity(TheaterRequest request, Theater theater) {
        theater.setName(request.getName());
        theater.setAddress(request.getAddress());
        theater.setContactInfo(request.getContactInfo());
        theater.setOpeningHours(request.getOpeningHours());
        theater.setStatus(request.getStatus());
    }
}

