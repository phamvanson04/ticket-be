package com.cinebee.presentation.controller;

import java.util.List;

import com.cinebee.presentation.dto.request.MovieRequest;
import com.cinebee.presentation.dto.response.BaseResponse;
import com.cinebee.presentation.dto.response.MovieResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<BaseResponse<String>> clearCache() {
        movieService.evictTrendingMoviesCache();
        return ResponseEntity.ok(BaseResponse.success("Trending movies cache cleared!", "Cache cleared"));
    }

    @GetMapping("/trending")
    public ResponseEntity<BaseResponse<List<MovieResponse>>> getTrendingMovies() {
        List<MovieResponse> trendingMovies = movieService.getTrendingMovies(10);
        return ResponseEntity.ok(BaseResponse.success(trendingMovies, "Fetched trending movies successfully"));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse<List<MovieResponse>>> searchSuggestMovies(
            @RequestParam String title) {

        List<MovieResponse> movies = movieService.searchTrendingMoviesByTitle(title, 0, 20);
        return ResponseEntity.ok(BaseResponse.success(movies, "Fetched search results successfully"));
    }


    @PostMapping("/add-new-film")
    public ResponseEntity<BaseResponse<MovieResponse>> addMovie(
            @RequestPart("info") String info,
            @RequestPart(value = "posterImageFile", required = false) MultipartFile posterImageFile) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MovieRequest req = mapper.readValue(info, MovieRequest.class);
        MovieResponse saved = movieService.addMovie(req, posterImageFile);
        return ResponseEntity.ok(BaseResponse.success(saved, "Movie created successfully"));
    }

    @PostMapping("/update-film")
    public ResponseEntity<BaseResponse<MovieResponse>> updateMovie(
            @RequestParam("id") Long id,
            @RequestPart("info") String info,
            @RequestPart(value = "posterImageFile", required = false) MultipartFile posterImageFile
    ) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        MovieRequest req = mapper.readValue(info, MovieRequest.class);
        MovieResponse updated = movieService.updateMovie(id, req, posterImageFile);
        return ResponseEntity.ok(BaseResponse.success(updated, "Movie updated successfully"));
    }

    @PostMapping("/delete-film")
    public ResponseEntity<BaseResponse<Void>> deleteMovie(@RequestParam Long id) {
        movieService.deleteMovie(id);
        return ResponseEntity.ok(BaseResponse.success(null, "Movie deleted successfully"));
    }

    @GetMapping("/list-movies")
    public ResponseEntity<BaseResponse<Object>> getAllMoviesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(BaseResponse.success(movieService.getAllMoviesPaged(page, size), "Fetched movies successfully"));
    }
}
