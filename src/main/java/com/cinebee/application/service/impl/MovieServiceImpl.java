package com.cinebee.application.service.impl;
import com.cinebee.presentation.dto.request.MovieRequest;
import com.cinebee.presentation.dto.response.MovieResponse;
import com.cinebee.domain.entity.Movie;
import com.cinebee.shared.exception.ApiException;
import com.cinebee.shared.exception.ErrorCode;
import com.cinebee.application.mapper.MovieMapper;
import com.cinebee.infrastructure.persistence.repository.MovieRepository;
import com.cinebee.application.service.MovieService;
import com.cinebee.shared.util.ServiceUtils;
import com.cinebee.domain.entity.Trailer;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
public class MovieServiceImpl implements MovieService {
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);
    private final MovieRepository movieRepository;
    @Autowired
    private Cloudinary cloudinary;

    public MovieServiceImpl(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Override
    @CacheEvict(value = "trendingMovies", allEntries = true)
    public void evictTrendingMoviesCache() {
        logger.info("Trending movies cache has been evicted.");
    }

    @Async
    private CompletableFuture<Map<String, Object>> uploadImageToCloudinary(MultipartFile imageFile) {
        try {
            @SuppressWarnings("unchecked") Map<String, Object> uploadResult = (Map<String, Object>) cloudinary.uploader().upload(imageFile.getBytes(), ObjectUtils.emptyMap());
            return CompletableFuture.completedFuture(uploadResult);
        } catch (Exception e) {
            logger.error("Failed to upload image to Cloudinary", e);
            throw new ApiException(ErrorCode.MOVIE_IMAGE_UPLOAD_FAILED, e);
        }
    }

    @Async
    private CompletableFuture<Void> deleteImageFromCloudinary(String publicId) {
        return CompletableFuture.runAsync(() -> {
            if (publicId != null && !publicId.isEmpty()) {
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                } catch (Exception e) {
                    logger.warn("Failed to delete image from Cloudinary with public ID: {}", publicId, e);
                    // Do not throw an exception here, as it might prevent the movie update from completing
                }
            }
        });
    }


    /**
     * Get a list of trending movies, sorted by rating, likes, and views.
     * @param limit the maximum number of movies to return
     * @return a list of MovieResponse objects representing the trending movies
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable("trendingMovies")
    public List<MovieResponse> getTrendingMovies(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Movie> page = movieRepository.findTrendingMovies(pageable);
        return IntStream.range(0, page.getContent().size())
                .mapToObj(i -> {
                    MovieResponse res = MovieMapper.mapToTrendingMovieResponse(page.getContent().get(i));
                    res.setRank(i + 1);
                    return res;
                })
                .toList();
    }

    /**
     * Search for trending movies by title.
     * @param title the title to search for
     * @param page the page number (0-indexed)
     * @param size the size of each page
     * @return a list of MovieResponse objects matching the search criteria
     */
    @Override
    @Transactional(readOnly = true)
    public List<MovieResponse> searchTrendingMoviesByTitle(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findByTitleContainingIgnoreCase(title, pageable)
                .getContent().stream().map(MovieMapper::mapToTrendingMovieResponse).toList();
    }

    /**
     * Add a new movie to the database.
     * @param req the MovieRequest containing movie details
     * @param posterImageFile the poster image file to upload
     * @return a MovieResponse representing the saved movie
     */

    @Override
    @Transactional
    public MovieResponse addMovie(MovieRequest req, MultipartFile posterImageFile) {
        Movie movie = MovieMapper.mapAddMovieRequestToEntity(req);
        if (posterImageFile != null && !posterImageFile.isEmpty()) {
            try {
                Map<String, Object> uploadResult = uploadImageToCloudinary(posterImageFile).get();
                movie.setPosterUrl((String) uploadResult.get("secure_url"));
                movie.setPosterPublicId((String) uploadResult.get("public_id"));
            } catch (Exception e) {
                throw new ApiException(ErrorCode.MOVIE_IMAGE_UPLOAD_FAILED, e);
            }
        }
        updateTrailer(movie, req.getTrailerUrl());
        return MovieMapper.mapToTrendingMovieResponse(movieRepository.save(movie));
    }

    /**
     * Update an existing movie in the database.
     * @param movieId the ID of the movie to update
     * @param req the MovieRequest containing updated movie details
     * @param posterImageFile the new poster image file to upload (optional)
     * @return a MovieResponse representing the updated movie
     */

    @Override
    @Transactional
    public MovieResponse updateMovie(Long movieId, MovieRequest req, MultipartFile posterImageFile) {
        Movie movie = ServiceUtils.findObjectOrThrow(() -> movieRepository.findById(movieId), ErrorCode.MOVIE_NOT_FOUND);
        MovieMapper.mapUpdateMovieRequestToEntity(req, movie);

        handlePosterImageUpdate(movie, req.getPosterUrl(), posterImageFile);
        updateTrailer(movie, req.getTrailerUrl());

        Movie updatedMovie = movieRepository.save(movie);
        return MovieMapper.mapToTrendingMovieResponse(updatedMovie);
    }

    private void updateTrailer(Movie movie, String trailerUrl) {
        if (StringUtils.hasText(trailerUrl)) {
            if (movie.getTrailer() != null) {
                // Update existing trailer URL
                movie.getTrailer().setTrailerUrl(trailerUrl);
            } else {
                // Create new trailer and associate it with the movie
                movie.setTrailer(new Trailer(trailerUrl));
            }
        } else {
            // If trailerUrl is empty or null, remove the existing trailer
            movie.setTrailer(null);
        }
    }

    private void handlePosterImageUpdate(Movie movie, String newPosterUrl, MultipartFile newPosterImageFile) {
        if (newPosterImageFile != null && !newPosterImageFile.isEmpty()) {
            // New file provided: delete old, upload new
            deleteImageFromCloudinary(movie.getPosterPublicId());
            try {
                Map<String, Object> uploadResult = uploadImageToCloudinary(newPosterImageFile).get();
                movie.setPosterUrl((String) uploadResult.get("secure_url"));
                movie.setPosterPublicId((String) uploadResult.get("public_id"));
            } catch (Exception e) {
                throw new ApiException(ErrorCode.MOVIE_IMAGE_UPLOAD_FAILED, e);
            }
        } else if (newPosterUrl != null && !newPosterUrl.isEmpty()) {
            // No new file, but a new URL is provided in request: update URL, delete old Cloudinary image if exists
            if (movie.getPosterPublicId() != null && !movie.getPosterPublicId().isEmpty()) {
                deleteImageFromCloudinary(movie.getPosterPublicId());
                movie.setPosterPublicId(null); // Clear public ID as it's no longer managed by Cloudinary
            }
            movie.setPosterUrl(newPosterUrl);
        } else if (newPosterUrl == null && movie.getPosterUrl() != null) {
            // Poster URL explicitly set to null in request, and there was an existing poster: delete old
            deleteImageFromCloudinary(movie.getPosterPublicId());
            movie.setPosterPublicId(null);
            movie.setPosterUrl(null);
        }
    }

    @Override
    @Transactional
    public void deleteMovie(Long id) {
        if (!movieRepository.existsById(id)) throw new ApiException(ErrorCode.MOVIE_NOT_FOUND);
        movieRepository.deleteById(id);
    }

    /**
     * Get a paginated list of all movies.
     * @param page the page number (0-indexed)
     * @param size the size of each page
     * @return a Page object containing Movie entities
     */
    @Override
    @Transactional(readOnly = true)
    public Page<Movie> getAllMoviesPaged(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findAll(pageable);
    }
}
