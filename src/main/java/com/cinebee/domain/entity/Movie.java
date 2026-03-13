package com.cinebee.domain.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Entity
@Getter
@Setter
@Table(name = "Movies")
public class Movie implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer duration;

    @Column(name = "poster_url", length = 255)
    private String posterUrl;

    @Column(name = "poster_public_id", length = 255)
    private String posterPublicId;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(length = 50)
    private String genre;

    @Column(name = "base_price", nullable = false)
    private Double basePrice = 0.0;

    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(length = 255)
    private String othernames;

    @Column
    private Double rating = 0.0;

    @Column
    private Integer votes = 0;


    @Column
    private Integer views = 0;
    @Column(length = 255)
    private String actors;

    @Column(length = 255)
    private String director;

    @Column(length = 100)
    private String country;

    @OneToOne(mappedBy = "movie", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Trailer trailer;

    // Helper method to manage bidirectional relationship
    public void setTrailer(Trailer trailer) {
        if (trailer == null) {
            if (this.trailer != null) {
                this.trailer.setMovie(null);
            }
        } else {
            trailer.setMovie(this);
        }
        this.trailer = trailer;
    }
}
