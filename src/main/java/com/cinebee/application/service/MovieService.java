package com.cinebee.application.service;

import com.cinebee.presentation.dto.request.MovieRequest;
import com.cinebee.presentation.dto.response.MovieResponse;
import com.cinebee.domain.entity.Movie;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface MovieService {
    List<MovieResponse> getTrendingMovies(int limit);
    List<MovieResponse> searchTrendingMoviesByTitle(String title, int page, int size);
    MovieResponse addMovie(MovieRequest req, MultipartFile posterImageFile);
    MovieResponse updateMovie(Long movieId, MovieRequest req, MultipartFile posterImageFile);
    void deleteMovie(Long id);
    Page<Movie> getAllMoviesPaged(int page, int size);
    void evictTrendingMoviesCache();
}

