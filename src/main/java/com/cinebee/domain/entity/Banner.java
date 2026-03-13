package com.cinebee.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@Table(name = "banners")
public class Banner implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String bannerUrl;
    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "is_active")
    private boolean isActive;

    // ThÃªm trÆ°á»ng priority Ä‘á»ƒ kiá»ƒm soÃ¡t thá»© tá»± hiá»ƒn thá»‹
    // Sá»‘ cÃ ng lá»›n cÃ ng Æ°u tiÃªn (banner má»›i sáº½ cÃ³ priority cao hÆ¡n)
    @Column(name = "priority")
    private Integer priority = 0;

}

