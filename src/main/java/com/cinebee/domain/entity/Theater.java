package com.cinebee.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Theaters")
@Getter
@Setter
@NoArgsConstructor
public class Theater {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String address;

    @Column
    private String contactInfo; // For phone number or email

    @Column
    private String openingHours; // e.g., "9:00 AM - 11:00 PM"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TheaterStatus status = TheaterStatus.ACTIVE;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(
        mappedBy = "theater",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    private List<Room> rooms = new ArrayList<>();

    public enum TheaterStatus {
        ACTIVE,
        INACTIVE,
        COMING_SOON,
        UNDER_MAINTENANCE
    }

    // Helper method to manage bidirectional relationship
    public void addRoom(Room room) {
        rooms.add(room);
        room.setTheater(this);
    }

    public void removeRoom(Room room) {
        rooms.remove(room);
        room.setTheater(null);
    }
}

