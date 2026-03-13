package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowtimeId(Long showtimeId);

    List<Seat> findByShowtimeIdAndIsAvailableTrue(Long showtimeId);

    Optional<Seat> findByShowtimeIdAndSeatNumber(Long showtimeId, String seatNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.showtime.id = :showtimeId AND s.seatNumber IN :seatNumbers")
    List<Seat> findByShowtimeIdAndSeatNumberInForUpdate(
            @Param("showtimeId") Long showtimeId,
            @Param("seatNumbers") List<String> seatNumbers
    );
}

