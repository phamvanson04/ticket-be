package com.cinebee.presentation.controller;

import java.util.List;

import com.cinebee.presentation.dto.request.MovieRequest;
import com.cinebee.presentation.dto.response.MovieResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestPart;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cinebee.application.service.MovieService;


@RestController
@RequestMapping("/api/movies")
public class MovieController {
    @Autowired
    private MovieService movieService;

    @GetMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        movieService.evictTrendingMoviesCache();
        return ResponseEntity.ok("Trending movies cache cleared!");
    }

    @GetMapping("/trending")
    public ResponseEntity<List<MovieResponse>> getTrendingMovies() {
        List<MovieResponse> trendingMovies = movieService.getTrendingMovies(10);
        return ResponseEntity.ok(trendingMovies);
    }

    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchSuggestMovies(
            @RequestParam String title) {

        List<MovieResponse> movies = movieService.searchTrendingMoviesByTitle(title, 0, 20);
        return ResponseEntity.ok(movies);
    }


    @PostMapping("/add-new-film")
    public ResponseEntity<?> addMovie(
            @RequestPart("info") String info,
            @RequestPart(value = "posterImageFile", required = false) MultipartFile posterImageFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MovieRequest req = mapper.readValue(info, MovieRequest.class);
        MovieResponse saved = movieService.addMovie(req, posterImageFile);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/update-film")
    public ResponseEntity<?> updateMovie(
            @RequestParam("id") Long id,
            @RequestPart("info") String info,
            @RequestPart(value = "posterImageFile", required = false) MultipartFile posterImageFile
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MovieRequest req = mapper.readValue(info, MovieRequest.class);
        MovieResponse updated = movieService.updateMovie(id, req, posterImageFile);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/delete-film")
    public ResponseEntity<?> deleteMovie(@RequestParam Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list-movies")
    public ResponseEntity<?> getAllMoviesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(movieService.getAllMoviesPaged(page, size));
    }
}
