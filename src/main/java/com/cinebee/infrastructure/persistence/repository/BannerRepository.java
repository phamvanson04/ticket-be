package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    List<Banner> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDescIdDesc(LocalDate start, LocalDate end);

    List<Banner> findByMovieId(Long movieId);
}

