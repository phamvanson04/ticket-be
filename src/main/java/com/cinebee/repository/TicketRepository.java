package com.cinebee.repository;

import com.cinebee.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT s.seatNumber FROM Ticket t JOIN t.seat s WHERE t.showtime.id = :showtimeId AND t.isCancelled = false")
    List<String> findBookedSeatsByShowtime(@Param("showtimeId") Long showtimeId);

    List<Ticket> findByUserId(Long userId);

    List<Ticket> findByShowtimeId(Long showtimeId);

    List<Ticket> findByUserIdAndShowtimeId(Long userId, Long showtimeId);

    List<Ticket> findByBookingReference(String bookingReference);

    boolean existsByShowtimeIdAndSeatIdAndIsCancelledFalse(Long showtimeId, Long seatId);

    long countByShowtimeId(Long showtimeId);
}
