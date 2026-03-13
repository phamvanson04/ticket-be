package com.cinebee.infrastructure.persistence.repository;

import com.cinebee.domain.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Page<Movie> findAll(Pageable pageable);

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // JPQL: Sáº¯p xáº¿p theo sá»‘ lÆ°á»£t Ä‘áº·t vÃ© (ticket), rating, views
    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.trailer tlr LEFT JOIN Showtime s ON s.movie = m LEFT JOIN Ticket t ON t.showtime = s GROUP BY m.id, tlr.id ORDER BY COUNT(t.id) DESC, m.rating DESC, m.views DESC")
    Page<Movie> findTrendingMovies(Pageable pageable);
}
