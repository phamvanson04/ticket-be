package com.cinebee.domain.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.cinebee.domain.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "Rooms", uniqueConstraints = @UniqueConstraint(columnNames = { "theater_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    @JsonBackReference // Prevents infinite loop in JSON serialization
    private Theater theater;

    @Column(nullable = false)
    private String name;

    private String type; // VIP, Standard, IMAX, etc.

    private Integer capacity;

    @Column(name = "is_active")
    private Boolean isActive = true;

}

