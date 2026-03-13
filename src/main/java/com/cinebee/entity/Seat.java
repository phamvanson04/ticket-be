package com.cinebee.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Seats", uniqueConstraints = @UniqueConstraint(columnNames = {"showtime_id", "seat_number"}))
@Getter
@Setter
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "showtime_id", nullable = false)
    private Showtime showtime;

    @Column(nullable = false)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomSeat.SeatType seatType = RoomSeat.SeatType.STANDARD;

    @Column(nullable = false)
    private Boolean isAvailable = true;

    @Column(nullable = false)
    private Double priceModifier = 1.0;

    @Version
    @Column(nullable = false)
    private Long version = 0L;
}
