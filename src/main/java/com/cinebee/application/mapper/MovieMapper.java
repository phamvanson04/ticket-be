package com.cinebee.application.mapper;

import com.cinebee.presentation.dto.request.MovieRequest;
import com.cinebee.presentation.dto.response.MovieResponse;
import com.cinebee.domain.entity.Movie;

import java.sql.Date;


/**
 * Utility class for mapping Movie entity to response DTOs.
 */
public class MovieMapper {
    /**
     * Maps a Movie entity to a MovieResponse DTO.
     * @param movie the Movie entity
     * @return MovieResponse DTO
     */
    public static MovieResponse mapToTrendingMovieResponse(Movie movie) {
        if (movie == null) return null;
        String trailerUrl = (movie.getTrailer() != null) ? movie.getTrailer().getTrailerUrl() : null;
        return new MovieResponse(
                movie.getId(),
                movie.getTitle(),
                movie.getOthernames(),
                movie.getRating(),
            movie.getPosterUrl(),
            0,
                movie.getDuration(),
                movie.getGenre(),
                movie.getActors(),
                movie.getDirector(),
                movie.getCountry(),
                movie.getReleaseDate() != null ? Date.valueOf(movie.getReleaseDate()).toLocalDate() : null,
                trailerUrl
        );
    }


    public static Movie mapAddMovieRequestToEntity(MovieRequest req) {
        if (req == null) return null;
        Movie movie = new Movie();
        movie.setTitle(req.getTitle());
        movie.setOthernames(req.getOthernames());
        movie.setBasePrice(req.getBasePrice() != null ? req.getBasePrice() : 0.0);
        movie.setDuration(req.getDuration());
        movie.setGenre(req.getGenre());
        movie.setPosterUrl(req.getPosterUrl());
        movie.setDescription(req.getDescription());
        movie.setActors(req.getActors());
        movie.setDirector(req.getDirector());
        movie.setCountry(req.getCountry());
        if (req.getReleaseDate() != null) {
            movie.setReleaseDate(req.getReleaseDate().toLocalDate());
        }
        movie.setDiscountPercentage(req.getDiscountPercentage() != null ? req.getDiscountPercentage() : 0.0);
        return movie;
    }

    public static void mapUpdateMovieRequestToEntity(MovieRequest req, Movie movie) {
        if (req == null || movie == null) return;
        if (req.getTitle() != null) movie.setTitle(req.getTitle());
        if (req.getOthernames() != null) movie.setOthernames(req.getOthernames());
        if (req.getBasePrice() != null) movie.setBasePrice(req.getBasePrice());
        if (req.getDuration() != null) movie.setDuration(req.getDuration());
        if (req.getGenre() != null) movie.setGenre(req.getGenre());
        if (req.getDescription() != null) movie.setDescription(req.getDescription());
        if (req.getActors() != null) movie.setActors(req.getActors());
        if (req.getDirector() != null) movie.setDirector(req.getDirector());
        if (req.getCountry() != null) movie.setCountry(req.getCountry());
        if (req.getReleaseDate() != null) {
            movie.setReleaseDate(req.getReleaseDate().toLocalDate());
        } else if (req.getReleaseDate() == null) {
            movie.setReleaseDate(null);
        }
        if (req.getDiscountPercentage() != null) movie.setDiscountPercentage(req.getDiscountPercentage());
    }

}
