package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {
    // Sáº¯p xáº¿p theo priority giáº£m dáº§n (priority cao lÃªn Ä‘áº§u), sau Ä‘Ã³ theo ID giáº£m dáº§n
    List<Banner> findByIsActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByPriorityDescIdDesc(LocalDate start, LocalDate end);
    
    // âœ¨ TÃ¬m banner theo movieId (Ä‘á»ƒ validate)
    List<Banner> findByMovieId(Long movieId);
}

