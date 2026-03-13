package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {

    // Find showtimes by movie
    List<Showtime> findByMovieIdAndStartTimeAfter(Long movieId, LocalDateTime startTime);
    
    // Find showtimes by theater
    List<Showtime> findByTheaterIdAndStartTimeAfter(Long theaterId, LocalDateTime startTime);
    
    // Find showtimes by movie and theater
    List<Showtime> findByMovieIdAndTheaterIdAndStartTimeAfter(Long movieId, Long theaterId, LocalDateTime startTime);
    
    // Find showtimes within date range
    List<Showtime> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // Find showtimes by movie within date range
    List<Showtime> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find showtimes by theater within date range
    List<Showtime> findByTheaterIdAndStartTimeBetween(Long theaterId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find showtimes by movie and theater within date range
    List<Showtime> findByMovieIdAndTheaterIdAndStartTimeBetween(Long movieId, Long theaterId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find today's showtimes for a theater
    @Query("SELECT s FROM Showtime s WHERE s.theater.id = :theaterId AND DATE(s.startTime) = DATE(:date)")
    List<Showtime> findByTheaterIdAndDate(@Param("theaterId") Long theaterId, @Param("date") LocalDateTime date);
    
    // Find upcoming showtimes for a movie
    @Query("SELECT s FROM Showtime s WHERE s.movie.id = :movieId AND s.startTime > :now ORDER BY s.startTime")
    List<Showtime> findUpcomingShowtimesByMovie(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);
    
    // Find showtimes by room
    List<Showtime> findByRoomIdAndStartTimeAfter(Long roomId, LocalDateTime startTime);
    
    // Find conflicting showtimes for room scheduling
    @Query("SELECT s FROM Showtime s WHERE s.room.id = :roomId AND " +
           "((s.startTime <= :endTime AND s.endTime >= :startTime))")
    List<Showtime> findConflictingShowtimes(@Param("roomId") Long roomId, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);
    
    // Find popular showtimes (with most bookings)
    @Query("SELECT s FROM Showtime s LEFT JOIN Ticket t ON t.showtime = s " +
           "WHERE s.startTime > :now " +
           "GROUP BY s.id " +
           "ORDER BY COUNT(t.id) DESC")
    List<Showtime> findPopularShowtimes(@Param("now") LocalDateTime now);
    
    // Count available seats for a showtime
    @Query("SELECT (s.room.capacity - COUNT(t.id)) FROM Showtime s LEFT JOIN Ticket t ON t.showtime = s " +
           "WHERE s.id = :showtimeId AND (t.isCancelled IS NULL OR t.isCancelled = false)")
    Integer countAvailableSeats(@Param("showtimeId") Long showtimeId);
}

