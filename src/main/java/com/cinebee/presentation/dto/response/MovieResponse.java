package com.cinebee.presentation.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MovieResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String othernames;
    private Double rating;
    private String img;
    private Integer rank;
    private Integer duration;
    private String genre;
    private String actors;
    private String director;
    private String country;
    private LocalDate releaseDate;
    private String trailerUrl;

    public MovieResponse(Long id, String title, String othernames, Double rating, String img, Integer rank, Integer duration, String genre, String actors, String director, String country, LocalDate releaseDate, String trailerUrl) {
        this.id = id;
        this.title = title;
        this.othernames = othernames;
        this.rating = rating;
        this.img = img;
        this.rank = rank;
        this.duration = duration;
        this.genre = genre;
        this.actors = actors;
        this.director = director;
        this.country = country;
        this.releaseDate = releaseDate;
        this.trailerUrl = trailerUrl;
    }
}

